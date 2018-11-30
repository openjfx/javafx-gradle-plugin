package org.openjfx.gradle;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
