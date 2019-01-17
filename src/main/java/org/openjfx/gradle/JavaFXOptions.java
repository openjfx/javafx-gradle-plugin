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

import java.util.ArrayList;
import java.util.List;

public class JavaFXOptions {

    private static final String MAVEN_JAVAFX_ARTIFACT_GROUP_ID = "org.openjfx";

    private final Project project;
    private final JavaFXPlatform platform;

    private String version = "11.0.2";
    private List<String> modules = new ArrayList<>();

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

        JavaFXModule.getJavaFXModules(this.modules).forEach(javaFXModule -> {
            project.getDependencies().add("implementation",
                    String.format("%s:%s:%s:%s", MAVEN_JAVAFX_ARTIFACT_GROUP_ID, javaFXModule.getArtifactName(),
                            getVersion(), getPlatform().getClassifier()));
        });
    }

    private void clearJavaFXDependencies() {
        var implementationConfiguration = project.getConfigurations().findByName("implementation");
        if (implementationConfiguration != null) {
            implementationConfiguration.getDependencies()
                    .removeIf(dependency -> MAVEN_JAVAFX_ARTIFACT_GROUP_ID.equals(dependency.getGroup()));
        }
    }
}
