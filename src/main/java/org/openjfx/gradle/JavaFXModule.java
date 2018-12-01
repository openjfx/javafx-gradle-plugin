package org.openjfx.gradle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

    public List<JavaFXModule> getDependentModules() {
        return dependentModules;
    }

    public List<JavaFXModule> getMavenDependencies() {
        List<JavaFXModule> dependencies = new ArrayList<>(dependentModules);
        dependencies.add(0, this);
        return dependencies;
    }
}
