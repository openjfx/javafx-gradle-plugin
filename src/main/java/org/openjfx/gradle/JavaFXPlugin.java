package org.openjfx.gradle;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleStmt;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.google.gradle.osdetector.OsDetector;
import com.google.gradle.osdetector.OsDetectorPlugin;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.impldep.aQute.bnd.osgi.Clazz;
import org.javamodularity.moduleplugin.ModuleSystemPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        project.getPlugins().apply(ApplicationPlugin.class);
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
            String version = project.getExtensions().getByType(JavaFXOptions.class).getVersion();
            List<String> modules = Collections.emptyList();
            String moduleName = (String) project.getExtensions().findByName("moduleName");
            if (moduleName != null) {
                Optional<ModuleDeclaration> moduleDeclaration = detectModule(project);
                if (moduleDeclaration.isPresent()) {
                    modules = moduleDeclaration.get().getModuleStmts().stream()
                            .filter(ModuleStmt::isModuleRequiresStmt)
                            .map(ModuleStmt::asModuleRequiresStmt)
                            .map(NodeWithName::getNameAsString)
                            .filter(JAVAFX_DEPENDENCIES::containsKey)
                            .collect(Collectors.toList());
                }
            } else {
                modules = project.getExtensions().getByType(JavaFXOptions.class).getModules();
            }

            project.getLogger().info("MODULES: " + modules);

            modules.stream().flatMap(m -> Stream.concat(Stream.of(m), JAVAFX_DEPENDENCIES.get(m).stream()))
                    .map(m -> m.replace(".", "-"))
                    .collect(Collectors.toSet()).forEach(m -> {
                        project.getDependencies().add("compile",
                                String.format("org.openjfx:%s:%s:%s", m, version, platform));
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
