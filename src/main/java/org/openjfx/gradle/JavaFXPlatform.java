/*
 * Copyright (c) 2018, Gluon
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

import com.google.gradle.osdetector.OsDetector;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum JavaFXPlatform {

    LINUX("linux", "linux-x86_64"),
    WINDOWS("win", "windows-x86_64"),
    OSX("mac", "osx-x86_64"),
    OSX_ARM("mac-aarch64", "osx-aarch_64");

    private String classifier;
    private String osDetectorId;

    JavaFXPlatform( String classifier, String osDetectorId ) {
        this.classifier = classifier;
        this.osDetectorId = osDetectorId;
    }

    public String getClassifier() {
        return classifier;
    }

    public static JavaFXPlatform detect(Project project) {

        String os = project.getExtensions().getByType(OsDetector.class).getClassifier();

        for ( JavaFXPlatform platform: values()) {
            if ( platform.osDetectorId.equals(os)) {
                return platform;
            }
        }

        String supportedPlatforms = Arrays.stream(values())
                .map(p->p.osDetectorId)
                .collect(Collectors.joining("', '", "'", "'"));

        throw new GradleException(
            String.format(
                    "Unsupported JavaFX platform found: '%s'! " +
                    "This plugin is designed to work on supported platforms only." +
                    "Current supported platforms are %s.", os, supportedPlatforms )
        );

    }
}
