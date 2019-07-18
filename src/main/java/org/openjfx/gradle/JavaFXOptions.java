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

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openjfx.gradle.JavaFXModule.PREFIX_MODULE;

public class JavaFXOptions {

    private static final String MAVEN_JAVAFX_ARTIFACT_GROUP_ID = "org.openjfx";

    private final Project project;
    private final JavaFXPlatform platform;

    private String version = "12.0.1";
    private String sdk;
    private String configuration = "implementation";
    private String lastUpdatedConfiguration;
    private List<String> modules = new ArrayList<>();
    private FlatDirectoryArtifactRepository sdkRepo;

    public JavaFXOptions(Project project) {
        this.project = project;
        this.platform = JavaFXPlatform.detect(project);
    }

    public JavaFXPlatform getPlatform() {
        return platform;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        updateJavaFXDependencies();
    }

    /**
     * If set, the JavaFX modules will be taken from this local
     * repository, and not from Maven Central
     * @param sdk, the path to the local JavaFX SDK/lib folder
     */
    public void setSdk(String sdk) {
        this.sdk = sdk;
        updateJavaFXDependencies();
    }

    public String getSdk() {
        return sdk;
    }

    /** Set the configuration name for dependencies, e.g.
     * 'implementation', 'compileOnly' etc.
     * @param configuration The configuration name for dependencies
     */
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
        updateJavaFXDependencies();
    }

    public String getConfiguration() {
        return configuration;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
        updateJavaFXDependencies();
    }

    public void modules(String...moduleNames) {
        setModules(List.of(moduleNames));
    }

    private void updateJavaFXDependencies() {
        clearJavaFXDependencies();

        String configuration = getConfiguration();
        JavaFXModule.getJavaFXModules(this.modules).forEach(javaFXModule -> {
            if (sdkRepo != null) {
                project.getDependencies().add(configuration, "name:" + javaFXModule.getModuleName());
            } else {
                project.getDependencies().add(configuration,
                        String.format("%s:%s:%s:%s", MAVEN_JAVAFX_ARTIFACT_GROUP_ID, javaFXModule.getArtifactName(),
                                getVersion(), getPlatform().getClassifier()));
            }
        });
        lastUpdatedConfiguration = configuration;
    }

    private void clearJavaFXDependencies() {
        if (sdkRepo != null) {
            project.getRepositories().remove(sdkRepo);
            sdkRepo = null;
        }

        if (sdk != null && ! sdk.isEmpty()) {
            Map<String, String> dirs = new HashMap<>();
            dirs.put("name", "sdkRepo");
            dirs.put("dirs", sdk);
            sdkRepo = project.getRepositories().flatDir(dirs);
        }

        if (lastUpdatedConfiguration == null) {
            return;
        }
        var configuration = project.getConfigurations().findByName(lastUpdatedConfiguration);
        if (configuration != null) {
            if (sdkRepo != null) {
                configuration.getDependencies()
                        .removeIf(dependency -> dependency.getName().startsWith(PREFIX_MODULE));
            } else {
                configuration.getDependencies()
                        .removeIf(dependency -> MAVEN_JAVAFX_ARTIFACT_GROUP_ID.equals(dependency.getGroup()));
            }
        }
    }
}
