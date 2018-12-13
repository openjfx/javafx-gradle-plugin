package org.openjfx.gradle;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class JavaFXModuleTest {

    @Test
    void existingModuleName() {
        Optional<JavaFXModule> javafxDependency = JavaFXModule.fromModuleName("javafx.base");
        assertTrue(javafxDependency.isPresent());
        assertEquals(JavaFXModule.BASE, javafxDependency.get());
    }

    @Test
    void nonExistingModuleName() {
        assertTrue(JavaFXModule.fromModuleName("javafx.unknown").isEmpty());
    }

    @Test
    void getModuleName() {
        assertEquals("javafx.base", JavaFXModule.BASE.getModuleName());
    }

    @Test
    void getArtifactName() {
        assertEquals("javafx-base", JavaFXModule.BASE.getArtifactName());
    }

    @Test
    void getDependencies() {
        JavaFXModule module = JavaFXModule.CONTROLS;

        List<JavaFXModule> dependencies = module.getMavenDependencies();
        assertTrue(dependencies.contains(module));
        for (JavaFXModule dependentModule : JavaFXModule.CONTROLS.getDependentModules()) {
            assertTrue(dependencies.contains(dependentModule));
        }
    }

    @Test
    void validateWithValidModules() {
        var moduleNames = List.of(JavaFXModule.CONTROLS.getModuleName(), JavaFXModule.WEB.getModuleName());

        JavaFXModule.validateModules(moduleNames);
    }

    @Test
    void validateWithInvalidModules() {
        var moduleNames = List.of("javafx.unknown", JavaFXModule.CONTROLS.getModuleName(), JavaFXModule.WEB.getModuleName());

        try {
            JavaFXModule.validateModules(moduleNames);
            fail("Validate Modules must throw GradleException.");
        } catch (GradleException e) {
            // expected
        }
    }
}
