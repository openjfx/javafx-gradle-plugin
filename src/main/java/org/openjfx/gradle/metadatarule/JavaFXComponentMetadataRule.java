/*
 * Copyright (c) 2023, Gluon
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
package org.openjfx.gradle.metadatarule;

import org.gradle.api.artifacts.CacheableRule;
import org.gradle.api.artifacts.ComponentMetadataContext;
import org.gradle.api.artifacts.ComponentMetadataDetails;
import org.gradle.api.artifacts.ComponentMetadataRule;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;
import org.openjfx.gradle.JavaFXPlatform;

import javax.inject.Inject;

import static org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE;
import static org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE;

/**
 * <a href="https://docs.gradle.org/current/userguide/component_metadata_rules.html#adding_variants_for_native_jars">
 *     Component Metadata RulesAdding variants for native jars
 * </a>
 */
@CacheableRule
abstract public class JavaFXComponentMetadataRule implements ComponentMetadataRule {

    @Inject
    abstract protected ObjectFactory getObjects();

    @Override
    public void execute(ComponentMetadataContext context) {
        var details = context.getDetails();

        for (JavaFXPlatform javaFXPlatform : JavaFXPlatform.values()) {
            addJavaFXPlatformVariant(javaFXPlatform, details, "Compile", "compile");
            addJavaFXPlatformVariant(javaFXPlatform, details, "Runtime", "runtime");
        }
    }

    private void addJavaFXPlatformVariant(JavaFXPlatform javaFXPlatform, ComponentMetadataDetails details, String nameSuffix, String baseVariant) {
        var name = details.getId().getName();
        var version = details.getId().getVersion();

        // Use 'maybeAddVariant'. As long as the metadata is sourced from POM, 'compile' and 'runtime' exist.
        // These are used as base for the additional variants so that those variants have the same dependencies.
        // If future JavaFX versions should publish the variants directly with Gradle metadata, the rule will
        // have no effect on these future versions, as the variants will be named different.
        details.maybeAddVariant(javaFXPlatform.getClassifier() + nameSuffix, baseVariant, variant -> {
            variant.attributes(attributes -> {
                attributes.attribute(OPERATING_SYSTEM_ATTRIBUTE, getObjects().named(OperatingSystemFamily.class, javaFXPlatform.getOsFamily()));
                attributes.attribute(ARCHITECTURE_ATTRIBUTE, getObjects().named(MachineArchitecture.class, javaFXPlatform.getArch()));
            });
            variant.withFiles(files -> {
                files.removeAllFiles();
                files.addFile(name + "-" + version + "-" + javaFXPlatform.getClassifier() + ".jar");
            });
        });
    }
}
