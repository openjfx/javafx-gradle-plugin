package org.openjfx.gradle;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleStmt;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.google.gradle.osdetector.OsDetector;
import com.google.gradle.osdetector.OsDetectorPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.javamodularity.moduleplugin.ModuleSystemPlugin;
import org.javamodularity.moduleplugin.tasks.ModuleOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
            var javafxModules = new TreeSet<>(project.getExtensions().getByType(JavaFXOptions.class).getModules());
            String moduleName = (String) project.getExtensions().findByName("moduleName");
            if (moduleName != null) {
                detectModule(project).ifPresent(moduleDeclaration1 ->
                        javafxModules.addAll(moduleDeclaration1.getModuleStmts().stream()
                                .filter(ModuleStmt::isModuleRequiresStmt)
                                .map(ModuleStmt::asModuleRequiresStmt)
                                .map(NodeWithName::getNameAsString)
                                .filter(JAVAFX_DEPENDENCIES::containsKey)
                                .collect(Collectors.toList())));
            }

            Set<String> uniqueJavaFXModules = javafxModules.stream()
                    .flatMap(javafxModule -> Stream.concat(Stream.of(javafxModule), JAVAFX_DEPENDENCIES.get(javafxModule).stream()))
                    .collect(Collectors.toSet());

            JavaExec execTask = (JavaExec) project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);
            if (execTask != null) {
                ModuleOptions moduleOptions = execTask.getExtensions().findByType(ModuleOptions.class);
                if (moduleOptions != null) {
                    uniqueJavaFXModules.forEach(m -> {
                        moduleOptions.getAddModules().add(m);
                    });
                }
            }

            String version = project.getExtensions().getByType(JavaFXOptions.class).getVersion();
            uniqueJavaFXModules.stream()
                    .map(module -> module.replace(".", "-"))
                    .forEach(artifact -> {
                project.getDependencies().add("compile",
                        String.format("org.openjfx:%s:%s:%s", artifact, version, platform));
            });
        });
    }

    private Optional<ModuleDeclaration> detectModule(Project project) {
        SourceSet main;
        try {
            JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
            main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        } catch (IllegalStateException | UnknownDomainObjectException e) {
            return Optional.empty();
        }

        Optional<File> moduleInfoSrcDir = main.getAllJava()
                .getSourceDirectories()
                .getFiles()
                .stream()
                .filter(dir -> dir.toPath().resolve("module-info.java").toFile().exists())
                .findAny();

        if (moduleInfoSrcDir.isPresent()) {
            Path moduleInfoJava = moduleInfoSrcDir.get().toPath().resolve("module-info.java");
            JavaParser.getStaticConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11);
            try {
                return JavaParser.parse(moduleInfoJava).getModule();
            } catch (IOException e) {
                project.getLogger().error("Error opening module-info.java in source dir {}", moduleInfoSrcDir.get());
            }
        }

        return Optional.empty();
    }
}
