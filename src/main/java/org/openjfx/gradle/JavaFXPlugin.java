package org.openjfx.gradle;

import com.google.gradle.osdetector.OsDetectorPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.JavaExec;
import org.javamodularity.moduleplugin.ModuleSystemPlugin;
import org.javamodularity.moduleplugin.tasks.ModuleOptions;

import java.util.TreeSet;

public class JavaFXPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(OsDetectorPlugin.class);
        project.getPlugins().apply(ModuleSystemPlugin.class);

        project.getExtensions().create("javafx", JavaFXOptions.class, project);

        updateApplicationRunTaskModules(project);
    }

    private void updateApplicationRunTaskModules(Project project) {
        project.afterEvaluate(c -> {
            JavaExec execTask = (JavaExec) project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);
            if (execTask != null) {
                ModuleOptions moduleOptions = execTask.getExtensions().findByType(ModuleOptions.class);
                if (moduleOptions != null) {
                    JavaFXOptions javaFXOptions = project.getExtensions().getByType(JavaFXOptions.class);
                    JavaFXModule.validateModules(javaFXOptions.getModules());

                    var definedJavaFXModuleNames = new TreeSet<>(javaFXOptions.getModules());

                    definedJavaFXModuleNames.forEach(javaFXModule -> moduleOptions.getAddModules().add(javaFXModule));
                }
            }
        });
    }
}
