package org.openjfx.gradle;

import com.google.gradle.osdetector.OsDetector;
import com.google.gradle.osdetector.OsDetectorPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.JavaExec;
import org.javamodularity.moduleplugin.ModuleSystemPlugin;
import org.javamodularity.moduleplugin.tasks.ModuleOptions;

import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class JavaFXPlugin implements Plugin<Project> {

    private String platform;

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(OsDetectorPlugin.class);
        project.getPlugins().apply(ModuleSystemPlugin.class);

        project.getExtensions().create("javafx", JavaFXOptions.class, project);

        detectPlatform(project);

        applyDependencies(project);
    }

    private void detectPlatform(Project project) {
        String os = project.getExtensions().getByType(OsDetector.class).getOs();
        platform = os;
        if ("osx".equals(os)) {
            platform = "mac";
        } else if ("windows".equals(os)) {
            platform = "win";
        }
    }

    private void applyDependencies(Project project) {
        project.afterEvaluate(c -> {
            JavaFXOptions javaFXOptions = project.getExtensions().getByType(JavaFXOptions.class);
            javaFXOptions.validateModules();

            var definedJavaFXModuleNames = new TreeSet<>(javaFXOptions.getModules());

            JavaExec execTask = (JavaExec) project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);
            if (execTask != null) {
                ModuleOptions moduleOptions = execTask.getExtensions().findByType(ModuleOptions.class);
                if (moduleOptions != null) {
                    definedJavaFXModuleNames.forEach(javaFXModule -> moduleOptions.getAddModules().add(javaFXModule));
                }
            }

            var javaFXModules = definedJavaFXModuleNames.stream()
                    .map(JavaFXModule::fromModuleName)
                    .flatMap(Optional::stream)
                    .flatMap(javaFXModule -> javaFXModule.getMavenDependencies().stream())
                    .collect(Collectors.toSet());

            javaFXModules.forEach(javaFXModule -> {
                project.getDependencies().add("implementation",
                        String.format("org.openjfx:%s:%s:%s", javaFXModule.getArtifactName(), javaFXOptions.getVersion(), platform));
            });
        });
    }
}
