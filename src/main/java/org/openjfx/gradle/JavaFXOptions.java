package org.openjfx.gradle;

import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.JavaExec;
import org.javamodularity.moduleplugin.tasks.ModuleOptions;

import java.util.ArrayList;
import java.util.List;

public class JavaFXOptions {

    private ModuleOptions moduleOptions;

    private String version = "11.0.1";
    private List<String> modules = new ArrayList<>();

    public JavaFXOptions(Project project) {
        JavaExec execTask = (JavaExec) project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);
        if (execTask != null) {
            this.moduleOptions = execTask.getExtensions().findByType(ModuleOptions.class);
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
        this.modules = modules;
        if (this.moduleOptions != null) {
            this.moduleOptions.getAddModules().addAll(this.modules);
        }
    }
}
