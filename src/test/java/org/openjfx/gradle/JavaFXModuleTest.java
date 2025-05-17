/*
 * Copyright (c) 2018, 2023, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjfx.gradle;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertFalse(JavaFXModule.fromModuleName("javafx.unknown").isPresent());
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
    void validateWithValidModules() {
        List<String> moduleNames = Arrays.asList(JavaFXModule.CONTROLS.getModuleName(), JavaFXModule.WEB.getModuleName());

        JavaFXModule.validateModules(moduleNames);
    }

    @Test
    void validateWithInvalidModules() {
        List<String> moduleNames = Arrays.asList("javafx.unknown", JavaFXModule.CONTROLS.getModuleName(), JavaFXModule.WEB.getModuleName());

        try {
            JavaFXModule.validateModules(moduleNames);
            fail("Validate Modules must throw GradleException.");
        } catch (GradleException e) {
            // expected
        }
    }
}
