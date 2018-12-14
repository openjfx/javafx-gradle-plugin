package org.openjfx.gradle;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

public class JavaFXOptions {

    private static final String MAVEN_JAVAFX_ARTIFACT_GROUP_ID = "org.openjfx";

    private final Project project;
    private final JavaFXPlatform platform;

    private String version = "11.0.1";
    private List<String> modules = new ArrayList<>();

    public JavaFXOptions(Project project) {
        this.project = project;
        this.platform = JavaFXPlatform.detect(project);//detectPlatform();
    }

    public JavaFXPlatform getPlatform() {
        return platform;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        updateJavaFXDependencies();
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
        updateJavaFXDependencies();
    }

    public void modules(String...moduleNames) {
        setModules(List.of(moduleNames));
    }

    private void updateJavaFXDependencies() {
        clearJavaFXDependencies();

        JavaFXModule.getJavaFXModules(this.modules).forEach(javaFXModule -> {
            project.getDependencies().add("implementation",
                    String.format("%s:%s:%s:%s", MAVEN_JAVAFX_ARTIFACT_GROUP_ID, javaFXModule.getArtifactName(),
                            getVersion(), getPlatform().getClassifier()));
        });
    }

    private void clearJavaFXDependencies() {
        var implementationConfiguration = project.getConfigurations().findByName("implementation");
        if (implementationConfiguration != null) {
            implementationConfiguration.getDependencies()
                    .removeIf(dependency -> MAVEN_JAVAFX_ARTIFACT_GROUP_ID.equals(dependency.getGroup()));
        }
    }
}
