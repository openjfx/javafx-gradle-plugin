package org.openjfx.gradle;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

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
}
