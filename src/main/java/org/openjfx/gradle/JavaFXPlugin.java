/*
 * Copyright (c) 2018, 2025, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjfx.gradle;

import com.google.gradle.osdetector.OsDetector;
import com.google.gradle.osdetector.OsDetectorPlugin;
import org.gradle.api.*;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.process.JavaForkOptions;
import org.gradle.util.GradleVersion;
import org.openjfx.gradle.metadatarule.JavaFXComponentMetadataRule;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.openjfx.gradle.JavaFXOptions.MAVEN_JAVAFX_ARTIFACT_GROUP_ID;

@NonNullApi
public class JavaFXPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (GradleVersion.current().compareTo(GradleVersion.version("6.4")) < 0) {
            throw new GradleException("This plugin requires Gradle 6.4+");
        }
        // Make sure 'java-base' is applied first, which makes the 'SourceSetContainer' available.
        // More concrete Java plugins that build on top of 'java-base' – like 'java-library' or 'application' –
        // will be applied by the user.
        project.getPlugins().apply(JavaBasePlugin.class);

        // Use 'OsDetectorPlugin' to select the platform the build runs on as default.
        project.getPlugins().apply(OsDetectorPlugin.class);

        JavaFXOptions javaFXOptions = project.getExtensions().create("javafx", JavaFXOptions.class,
                project.getExtensions().getByType(SourceSetContainer.class),
                project.getExtensions().getByType(OsDetector.class));

        // Register 'JavaFXComponentMetadataRule' to add variant information to all JavaFX modules.
        // Future JavaFX versions could publish this information using Gradle Metadata.
        for (JavaFXModule javaFXModule: JavaFXModule.values()) {
            project.getDependencies().getComponents().withModule(
                    MAVEN_JAVAFX_ARTIFACT_GROUP_ID + ":" + javaFXModule.getArtifactName(),
                    JavaFXComponentMetadataRule.class);
        }

        // Only set the default 'configuration' to 'implementation' when the 'java' plugin is applied.
        // Otherwise, it won't exist. (Note: 'java' is implicitly applied by 'application' or 'java-library'
        // and other Java-base plugins like Kotlin JVM)
        project.getPlugins().withId("java", p -> javaFXOptions.setConfiguration("implementation"));


        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                if (project.getPlugins().hasPlugin("org.javamodularity.moduleplugin") && project.getExtensions().findByName("modulename") != null) {
                    return;
                }

                for (String taskName : javaFXOptions.getModulePathTasks()) {
                    // Only the 'run' task depends on the 'application' plugin, other tasks are configured explicitly.
                    if (taskName.equals(ApplicationPlugin.TASK_RUN_NAME) && !project.getPlugins().hasPlugin("application")) {
                        continue;
                    }

                    project.getTasks().named(taskName, new Action<Task>() {
                        @Override
                        public void execute(Task task) {
                            final var fxPlatform = javaFXOptions.getFxPlatform();
                            final var fxModules = javaFXOptions.getFxModules();
                            if (task instanceof JavaExec) {
                                JavaExec execTask = (JavaExec) task;
                                if (execTask.getMainModule().isPresent()) {
                                    return;
                                }
                                execTask.doFirst(a -> {
                                    putJavaFXJarsOnModulePath(execTask, execTask.getClasspath(), execTask::setClasspath, fxPlatform, fxModules);
                                });
                            } else if (task instanceof Test) {
                                Test testTask = (Test) task;
                                testTask.doFirst(a -> {
                                    if (isModularTest(testTask)) {
                                        return;
                                    }
                                    putJavaFXJarsOnModulePath(testTask, testTask.getClasspath(), testTask::setClasspath, fxPlatform, fxModules);
                                });
                            } else {
                                throw new GradleException("Task '" + task.getPath() + "' in 'javafx.modulePathTasks' has to be a JavaExec or Test task");
                            }
                        }
                    });
                }

            }
        });

    }

    /**
     * Gradle does currently not put anything on the --module-path if the application (or test) itself is executed from
     * the classpath. Hence, this patches the setup of the configured tasks (default: ':run') to move all JavaFX Jars
     * from '-classpath' to '-module-path'. This functionality is only relevant for NON-MODULAR apps.
     */
    private static void putJavaFXJarsOnModulePath(JavaForkOptions task, FileCollection classpath, Consumer<FileCollection> setClasspath, final Property<JavaFXPlatform> platform, final SetProperty<String> stringSetProperty) {
        setClasspath.accept(classpath.filter(jar -> !isJavaFXJar(jar, platform.get())));
        var modulePath = classpath.filter(jar -> isJavaFXJar(jar, platform.get()));

        task.getJvmArgumentProviders().add(() -> List.of(
                "--module-path", modulePath.getAsPath(),
                "--add-modules", String.join(",", stringSetProperty.get())
        ));
    }

    /**
     * Same detection as Gradle uses: Tests run as module if the module path gets inferred
     * and the test classes contain a module descriptor. Then Gradle takes care of the module path itself.
     */
    private static boolean isModularTest(Test task) {
        return task.getModularity().getInferModulePath().get() &&
                task.getTestClassesDirs().getFiles().stream().anyMatch(dir -> new File(dir, "module-info.class").isFile());
    }

    private static boolean isJavaFXJar(File jar, JavaFXPlatform platform) {
        return jar.isFile() &&
                Arrays.stream(JavaFXModule.values()).anyMatch(javaFXModule ->
                        javaFXModule.compareJarFileName(platform, jar.getName()) ||
                                javaFXModule.getModuleJarFileName().equals(jar.getName()));
    }
}
