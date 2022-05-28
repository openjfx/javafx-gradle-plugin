/*
 * Copyright (c) 2018, Gluon
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
package org.openjfx.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.*;
import org.javamodularity.moduleplugin.extensions.RunModuleOptions;
import org.openjfx.gradle.JavaFXModule;
import org.openjfx.gradle.JavaFXOptions;
import org.openjfx.gradle.JavaFXPlatform;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class JavaFXTask extends DefaultTask {

    private static final Logger LOGGER = Logging.getLogger(JavaFXTask.class);

    private JavaFXOptions options = null;

    protected final Project project;

    private JavaExec execTask;
    @Inject
    public JavaFXTask(final Project project) {
        this.project = project;

        project.getPluginManager().withPlugin(ApplicationPlugin.APPLICATION_PLUGIN_NAME, e -> {
            execTask = (JavaExec) project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);

            if (execTask == null) {
                throw new GradleException("Run task not found.");
            }

            execTask.dependsOn(this);
        });
    }

    /**
     * Retrieve and validate the JavaFXOptions instance for this task.
     *
     * @return JavaFXOptions instance
     */
    @Internal
    public JavaFXOptions getJavaFXOptions() {
        if (options == null) {
            options = project.getExtensions().getByType(JavaFXOptions.class);
            JavaFXModule.validateModules(options.getModules());
        }

        return options;
    }

    /**
     * Returns a set of the defined modules for this task.
     *
     * @return Set of requested JavaFX modules
     */
    @Internal
    public Set<String> getModules() {
        return Set.copyOf(getJavaFXOptions().getModules());
    }

    /**
     * Filters out any JavaFX jars from the passed in FileCollection.
     *
     * @param classpath The classpath to filter.
     *
     * @return FileCollection containing non-JavaFX jars
     */
    public FileCollection removeJavaFXJars(final FileCollection classpath) {
        return classpath.filter(
                jar -> Arrays.stream(JavaFXModule.values()).noneMatch(
                        javaFXModule -> jar.getName().contains(javaFXModule.getArtifactName())
                )
        );
    }

    /**
     * Filters out anything that is not a JavaFX jar from the the passed in FileCollection.
     *
     * @param classpath The classpath to filter.
     *
     * @return FileCollection containing only JavaFX platform jars.
     */
    public FileCollection onlyJavaFXPlatformJars(final FileCollection classpath) {
        return classpath.filter(jar -> isJavaFXJar(jar, getJavaFXOptions().getPlatform()));
    }

    /**
     * Generates a classpath with the only the non-JavaFX and JavaFX platform jars.
     * This is used whan configuration Modular JavaFX applications.
     *
     * @param classpath The classpath to filter.
     *
     * @return FileCollections with "empty" JavaFX jars removed but platform jars present.
     */
    public FileCollection filterEmptyJavaFXJArs(final FileCollection classpath) {
        return removeJavaFXJars(classpath).plus(onlyJavaFXPlatformJars(classpath));
    }

    /**
     * Generate extra jvm options for non-modular java applications.
     *
     * @param classpath    The classpath to use when generating jvm options.
     * @param originalOpts Any options that need to be retained from the original configuration.
     *
     * @return List of jvm arguments
     */
    public List<String> buildNonModularJvmOptions(final FileCollection classpath, final List<String> originalOpts) {
        var javaFXModuleJvmArgs = List.of("--module-path", onlyJavaFXPlatformJars(classpath).getAsPath());

        var jvmArgs = new ArrayList<String>();
        jvmArgs.add("--add-modules");
        jvmArgs.add(String.join(",", getModules()));

        if (originalOpts != null) {
            jvmArgs.addAll(originalOpts);
        }
        jvmArgs.addAll(javaFXModuleJvmArgs);

        return jvmArgs;
    }

    static boolean isJavaFXJar(final File jar, final JavaFXPlatform platform) {
        return jar.isFile() &&
                Arrays.stream(JavaFXModule.values()).anyMatch(javaFXModule ->
                    javaFXModule.compareJarFileName(platform, jar.getName()) ||
                    javaFXModule.getModuleJarFileName().equals(jar.getName()));
    }

    /**
     * Retrieves a configured JavaExec task instance. This is requried for pretty much all subclasses. If no JavaExec
     * task is found, throw an exception.
     *
     * @return JavaExec task
     */
    @Internal
    public JavaExec getExecTask() {
        if (execTask == null) {
            throw new GradleException("Run task not found. Please, make sure the Application plugin is applied");
        }
        return execTask;
    }

    @TaskAction
    public void action() {
        final JavaExec execTask = getExecTask();

        final Set<String> definedJavaFXModuleNames = getModules();
        if (definedJavaFXModuleNames.isEmpty()) {
            // If definedJavaFXModuleNames is empty, there is nothing for this code to do.
            return;
        }

        final RunModuleOptions moduleOptions = execTask.getExtensions().findByType(RunModuleOptions.class);
        final FileCollection originalClasspath = getClasspath();

        if (moduleOptions != null) {
            LOGGER.info("Modular JavaFX application found");

            // Remove empty JavaFX jars from classpath
            setTargetClasspath(filterEmptyJavaFXJArs(originalClasspath));
            definedJavaFXModuleNames.forEach(javaFXModule -> moduleOptions.getAddModules().add(javaFXModule));
        } else {
            LOGGER.info("Non-modular JavaFX application found");

            // Remove all JavaFX jars from classpath
            setTargetClasspath(removeJavaFXJars(originalClasspath));
            setTargetJvmArgs(buildNonModularJvmOptions(originalClasspath, getTargetJvmArgs()));
        }
    }

    /**
     * Implemented by subclasses to retrieve the original classpath before modification.
     *
     * @return FileCollection containing the original classpath.
     */
    @Internal
    abstract FileCollection getClasspath();

    /**
     * Implemented by subclasses to set classpath values on target task.
     *
     * @param classpath FileCollection to set as classpath.
     */
    @Internal
    public abstract void setTargetClasspath(final FileCollection classpath);

    /**
     * Implemented by subclasses to set required JvmArgs on target task.
     *
     * @param jvmArgs List of Jvm arguments to set on target task.
     */
    @Internal
    public abstract void setTargetJvmArgs(final List<String> jvmArgs);

    /**
     * Returns the existing list of JvmArgs for the target task.
     *
     * @return List or previously configured Jvm arguments.
     */
    @Internal
    public abstract List<String> getTargetJvmArgs();
}
