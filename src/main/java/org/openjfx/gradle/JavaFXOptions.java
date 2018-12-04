package org.openjfx.gradle;

import com.google.gradle.osdetector.OsDetector;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.TreeSet;

public class JavaFXOptions {

    private String version = "11.0.1";
    private final List<String> modules = new ArrayList<>();
    private final Project project;
    private String platform;

    public JavaFXOptions(Project project) {
	this.project = project;
        detectPlatform(project);
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
	this.modules.clear();
        this.modules.addAll(modules);

	validateModules();

	var definedJavaFXModuleNames = new TreeSet<>(getModules());

        var javaFXModules = definedJavaFXModuleNames.stream()
                    .map(JavaFXModule::fromModuleName)
                    .flatMap(Optional::stream)
                    .flatMap(javaFXModule -> javaFXModule.getMavenDependencies().stream())
                    .collect(Collectors.toSet());

        javaFXModules.forEach(javaFXModule -> {
                project.getDependencies().add("implementation",
                        String.format("org.openjfx:%s:%s:%s", javaFXModule.getArtifactName(), getVersion(), platform));
	    });
    }

    public void modules(String...moduleNames) {
        setModules(List.of(moduleNames));
    }

    public void validateModules() {
        List<String> invalidModules = this.modules.stream()
                .filter(module -> JavaFXModule.fromModuleName(module).isEmpty())
                .collect(Collectors.toList());

        if (! invalidModules.isEmpty()) {
            throw new GradleException("Found one or more invalid JavaFX module names: " + invalidModules);
        }
    }
}
