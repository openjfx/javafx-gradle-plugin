/*
 * Copyright (c) 2018, 2025, Gluon
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
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

abstract public class JavaFXOptions {

    static final String MAVEN_JAVAFX_ARTIFACT_GROUP_ID = "org.openjfx";
    private static final String JAVAFX_SDK_LIB_FOLDER = "lib";
    private final SetProperty<String> modules;
    private final Property<JavaFXPlatform> platform;


    private String version = "17";
    private String sdk;
    private String[] configurations = new String[] { "implementation" };
    private FlatDirectoryArtifactRepository customSDKArtifactRepository;

    private final SourceSetContainer sourceSets;
    private final Set<String> seenConfigurations = new HashSet<>();

    @Inject
    abstract protected ObjectFactory getObjects();

    @Inject
    abstract protected RepositoryHandler getRepositories();

    @Inject
    abstract protected ConfigurationContainer getConfigurationContainer();

    @Inject
    abstract protected DependencyHandler getDependencies();

    public JavaFXOptions(SourceSetContainer sourceSets, OsDetector osDetector) {
        this.sourceSets = sourceSets;
        platform = getObjects().property(JavaFXPlatform.class);
        getFxPlatform().convention(JavaFXPlatform.detect(osDetector));
        setClasspathAttributesForAllSourceSets();
        modules = getObjects().setProperty(String.class);
    }


    public JavaFXPlatform getPlatform() {
        return getFxPlatform().get();
    }

    public  Property<JavaFXPlatform> getFxPlatform(){
        return platform;
    }

    /**
     * Sets the target platform for the dependencies.
     * @param platform platform classifier.
     * Supported classifiers are linux, linux-aarch64, win/windows, osx/mac/macos or osx-aarch64/mac-aarch64/macos-aarch64.
     */
    public void setPlatform(String platform) {
        this.getFxPlatform().set(JavaFXPlatform.fromString(platform));
        setClasspathAttributesForAllSourceSets();
    }

    private void setClasspathAttributesForAllSourceSets() {
        sourceSets.all(sourceSet -> {
            setClasspathAttributes(getConfigurationContainer().getByName(sourceSet.getCompileClasspathConfigurationName()));
            setClasspathAttributes(getConfigurationContainer().getByName(sourceSet.getRuntimeClasspathConfigurationName()));
        });
    }

    private void setClasspathAttributes(Configuration classpath) {
        classpath.getAttributes().attribute(
                OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE,
                getObjects().named(OperatingSystemFamily.class, platform.get().getOsFamily()));
        classpath.getAttributes().attribute(
                MachineArchitecture.ARCHITECTURE_ATTRIBUTE,
                getObjects().named(MachineArchitecture.class, platform.get().getArch()));
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * If set, the JavaFX modules will be taken from this local
     * repository, and not from Maven Central
     * @param sdk, the path to the local JavaFX SDK folder
     */
    public void setSdk(String sdk) {
        this.sdk = sdk;
        updateCustomSDKArtifactRepository();
    }

    public String getSdk() {
        return sdk;
    }

    /**
     * Set the configuration name for dependencies, e.g.
     * 'implementation', 'compileOnly' etc.
     * @param configuration The configuration name for dependencies
     */
    public void setConfiguration(String configuration) {
        setConfigurations(new String[] { configuration });
    }

    /**
     * Set the configurations for dependencies, e.g.
     * 'implementation', 'compileOnly' etc.
     * @param configurations List of configuration names
     */
    public void setConfigurations(String[] configurations) {
        this.configurations = configurations;
        for (String conf : configurations) {
            if (!seenConfigurations.contains(conf)) {
                declareFXDependencies(conf);
                seenConfigurations.add(conf);
            }
        }
    }

    public String getConfiguration() {
        return configurations[0];
    }

    public String[] getConfigurations() {
        return configurations;
    }

    public SetProperty<String> getFxModules() {
        return modules;
    }

    public Set<String> getModules() {
        return modules.get();
    }

    public void setModules(List<String> modules) {
        this.modules.set(modules);

    }

    public void modules(String...moduleNames) {
        setModules(List.of(moduleNames));
    }

    private void declareFXDependencies(String conf) {
        // Use 'withDependencies' to declare the dependencies late (i.e., right before dependency resolution starts).
        // This allows users to make multiple modifications to the 'configurations' list at arbitrary times during
        // build configuration.
        getConfigurationContainer().getByName(conf).withDependencies(dependencySet -> {
            if (!List.of(configurations).contains(conf)) {
                // configuration was removed: do nothing
                return;
            }

            var javaFXModules = JavaFXModule.getJavaFXModules(getModules());
            if (customSDKArtifactRepository == null) {
                javaFXModules.stream()
                        .sorted()
                        .forEach(javaFXModule ->
                                dependencySet.add(getDependencies().create(
                                        MAVEN_JAVAFX_ARTIFACT_GROUP_ID + ":" +
                                                javaFXModule.getArtifactName() + ":" +
                                                getVersion())));
            } else {
                // Use the list of dependencies of each module to also add direct dependencies for those.
                // This is needed, because there is no information about transitive dependencies in the metadata
                // of the modules (as there is no such metadata in the local sdk).
                var javaFXModulesWithTransitives = Stream.concat(
                                javaFXModules.stream(),
                                javaFXModules.stream()
                                        .flatMap(m -> m.getDependentModules().stream()))
                        .distinct()
                        .sorted();

                javaFXModulesWithTransitives.forEach(javaFXModule ->
                        dependencySet.add(getDependencies().create(
                                Map.of("name", javaFXModule.getModuleName()))));
            }
        });
    }

    private void updateCustomSDKArtifactRepository() {
        if (customSDKArtifactRepository != null) {
            getRepositories().remove(customSDKArtifactRepository);
            customSDKArtifactRepository = null;
        }

        if (sdk != null && !sdk.isEmpty()) {
            Map<String, String> dirs = new HashMap<>();
            dirs.put("name", "customSDKArtifactRepository");
            if (sdk.endsWith(File.separator)) {
                dirs.put("dirs", sdk + JAVAFX_SDK_LIB_FOLDER);
            } else {
                dirs.put("dirs", sdk + File.separator + JAVAFX_SDK_LIB_FOLDER);
            }
            customSDKArtifactRepository = getRepositories().flatDir(dirs);
        }
    }
}
