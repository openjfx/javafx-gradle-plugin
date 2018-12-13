package org.openjfx.gradle;

import org.gradle.api.GradleException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum JavaFXModule {

    BASE,
    GRAPHICS(BASE),
    CONTROLS(BASE, GRAPHICS),
    FXML(BASE, GRAPHICS),
    MEDIA(BASE, GRAPHICS),
    SWING(BASE, GRAPHICS),
    WEB(BASE, CONTROLS, GRAPHICS, MEDIA);

    private static final String PREFIX_MODULE = "javafx.";
    private static final String PREFIX_ARTIFACT = "javafx-";

    private List<JavaFXModule> dependentModules;

    JavaFXModule(JavaFXModule...dependentModules) {
        this.dependentModules = List.of(dependentModules);
    }

    public static Optional<JavaFXModule> fromModuleName(String moduleName) {
        return Stream.of(JavaFXModule.values())
                .filter(javaFXModule -> moduleName.equals(javaFXModule.getModuleName()))
                .findFirst();
    }

    public String getModuleName() {
        return PREFIX_MODULE + name().toLowerCase(Locale.ROOT);
    }

    public String getArtifactName() {
        return PREFIX_ARTIFACT + name().toLowerCase(Locale.ROOT);
    }

    public static Set<JavaFXModule> getJavaFXModules(List<String> moduleNames) {
        validateModules(moduleNames);

        return moduleNames.stream()
                .map(JavaFXModule::fromModuleName)
                .flatMap(Optional::stream)
                .flatMap(javaFXModule -> javaFXModule.getMavenDependencies().stream())
                .collect(Collectors.toSet());
    }

    public static void validateModules(List<String> moduleNames) {
        var invalidModules = moduleNames.stream()
                .filter(module -> JavaFXModule.fromModuleName(module).isEmpty())
                .collect(Collectors.toList());

        if (! invalidModules.isEmpty()) {
            throw new GradleException("Found one or more invalid JavaFX module names: " + invalidModules);
        }
    }

    public List<JavaFXModule> getDependentModules() {
        return dependentModules;
    }

    public List<JavaFXModule> getMavenDependencies() {
        List<JavaFXModule> dependencies = new ArrayList<>(dependentModules);
        dependencies.add(0, this);
        return dependencies;
    }
}
