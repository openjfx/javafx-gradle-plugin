package org.openjfx.gradle;

import com.google.gradle.osdetector.OsDetector;
import com.google.gradle.osdetector.OsDetectorPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.JavaExec;
import org.javamodularity.moduleplugin.ModuleSystemPlugin;
import org.javamodularity.moduleplugin.tasks.ModuleOptions;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaFXPlugin implements Plugin<Project> {

    private Map<String, List<String>> JAVAFX_DEPENDENCIES = Map.of(
            "javafx.base", List.of(),
            "javafx.controls", List.of("javafx.base", "javafx.graphics"),
            "javafx.fxml", List.of("javafx.base", "javafx.graphics"),
            "javafx.graphics", List.of("javafx.base"),
            "javafx.media", List.of("javafx.base", "javafx.graphics"),
            "javafx.swing", List.of("javafx.base", "javafx.graphics"),
            "javafx.web", List.of("javafx.base", "javafx.controls", "javafx.graphics", "javafx.media")
    );

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
            var definedJavaFXModules = new TreeSet<>(project.getExtensions().getByType(JavaFXOptions.class).getModules());

            JavaExec execTask = (JavaExec) project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);
            if (execTask != null) {
                ModuleOptions moduleOptions = execTask.getExtensions().findByType(ModuleOptions.class);
                if (moduleOptions != null) {
                    definedJavaFXModules.forEach(javaFXModule -> moduleOptions.getAddModules().add(javaFXModule));
                }
            }

            var allJavaFXModules = definedJavaFXModules.stream()
                    .flatMap(javaFXModule -> Stream.concat(Stream.of(javaFXModule), JAVAFX_DEPENDENCIES.get(javaFXModule).stream()))
                    .collect(Collectors.toSet());

            String javaFXVersion = project.getExtensions().getByType(JavaFXOptions.class).getVersion();
            allJavaFXModules.stream()
                    .map(javaFXModule -> javaFXModule.replace(".", "-"))
                    .forEach(javaFXArtifact -> {
                project.getDependencies().add("compile",
                        String.format("org.openjfx:%s:%s:%s", javaFXArtifact, javaFXVersion, platform));
            });
        });
    }
}
