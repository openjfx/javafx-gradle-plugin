package org.openjfx.gradle;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JavaFXOptions {

    private String version = "11.0.1";
    private List<String> modules = new ArrayList<>();

    public JavaFXOptions(Project project) {
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
        this.modules = modules;
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
