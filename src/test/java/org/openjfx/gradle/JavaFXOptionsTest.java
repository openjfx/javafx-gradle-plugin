package org.openjfx.gradle;

import org.gradle.api.GradleException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

class JavaFXOptionsTest {

    @Test
    void validateWithValidModules() {
        JavaFXOptions options = new JavaFXOptions(ProjectBuilder.builder().build());
        options.setModules(List.of(JavaFXModule.CONTROLS.getModuleName(), JavaFXModule.WEB.getModuleName()));

        options.validateModules();
    }

    @Test
    void validateWithInvalidModules() {
        JavaFXOptions options = new JavaFXOptions(ProjectBuilder.builder().build());
        options.setModules(List.of("javafx.unknown", JavaFXModule.CONTROLS.getModuleName(), JavaFXModule.WEB.getModuleName()));

        try {
            options.validateModules();
            fail("Validate Modules must throw GradleException.");
        } catch (GradleException e) {
            // expected
        }
    }
}
