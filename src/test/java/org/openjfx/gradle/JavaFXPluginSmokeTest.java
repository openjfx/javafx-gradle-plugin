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

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class JavaFXPluginSmokeTest {

    private static final String classifier;

    static {
        Project p = ProjectBuilder.builder().build();
        p.getPlugins().apply(JavaFXPlugin.class);
        classifier = p.getExtensions().getByType(JavaFXOptions.class).getPlatform().getClassifier();
    }

    protected abstract String getGradleVersion();

    protected String modularApplicationRuntime() {
        return "modular-with-modularity-plugin.jar";
    }

    @Test
    void smokeTestModular() {
        BuildResult result = buildWithJava11(":modular:run");

        assertEquals(TaskOutcome.SUCCESS, result.task(":modular:run").getOutcome());

        assertEquals(Arrays.asList("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar"), modulePath(result).get(0));
        assertEquals(Arrays.asList("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar", "modular.jar"), modulePath(result).get(1));

        assertEquals(Arrays.asList(), compileClassPath(result));
        assertEquals(Arrays.asList(), runtimeClassPath(result));
    }

    @Test
    void smokeTestModularWithModularityPlugin() {
        BuildResult result = buildWithJava11(":modular-with-modularity-plugin:run");

        assertEquals(TaskOutcome.SUCCESS, result.task(":modular-with-modularity-plugin:run").getOutcome());

        assertEquals(Arrays.asList("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar"), modulePath(result).get(0));
        assertEquals(Arrays.asList("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar", modularApplicationRuntime()), modulePath(result).get(1));

        assertEquals(Arrays.asList(), compileClassPath(result));
        assertEquals(Arrays.asList(), runtimeClassPath(result));
    }

    @Test
    void smokeTestNonModular() {
        BuildResult result = buildWithJava11(":non-modular:run");

        assertEquals(TaskOutcome.SUCCESS, result.task(":non-modular:run").getOutcome());

        assertEquals(Arrays.asList("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar", "javafx-media-17-" + classifier + ".jar", "javafx-web-17-" + classifier + ".jar"), compileClassPath(result).get(0));
        assertEquals(Arrays.asList("main", "main"), runtimeClassPath(result).get(0));
        assertEquals(Arrays.asList("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar", "javafx-media-17-" + classifier + ".jar", "javafx-web-17-" + classifier + ".jar"), modulePath(result).get(0));
    }

    @Test
    void smokeTestTransitive() {
        BuildResult result = buildWithJava11(":transitive:run");

        assertEquals(TaskOutcome.SUCCESS, result.task(":transitive:run").getOutcome());

        assertEquals(Arrays.asList("charts-17.1.41.jar", "javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar"), compileClassPath(result).get(0));
        assertEquals(Arrays.asList("charts-17.1.41.jar", "countries-17.0.29.jar", "heatmap-17.0.17.jar", "logback-classic-1.2.6.jar", "logback-core-1.2.6.jar", "main", "main", "slf4j-api-1.7.32.jar", "toolbox-17.0.45.jar", "toolboxfx-17.0.37.jar"), runtimeClassPath(result).get(0));
        assertEquals(Arrays.asList("javafx-base-17.0.6-" + classifier + ".jar", "javafx-controls-17.0.6-" + classifier + ".jar", "javafx-graphics-17.0.6-" + classifier + ".jar", "javafx-swing-17.0.6-" + classifier + ".jar"), modulePath(result).get(0));
    }

    @Test
    void smokeTestLocalSdk() {
        BuildResult result = buildWithJava11(":local-sdk:build"); // do not ':run', as it won't run on any platform

        assertEquals(TaskOutcome.SUCCESS, result.task(":local-sdk:build").getOutcome());

        assertEquals(Arrays.asList("javafx.base.jar", "javafx.controls.jar", "javafx.graphics.jar"), compileClassPath(result).get(0));
        assertEquals(Arrays.asList(), modulePath(result));
    }

    private static List<List<String>> modulePath(BuildResult result) {
        return path(result, "--module-path ");
    }

    private static List<List<String>> compileClassPath(BuildResult result) {
        return path(result, "-classpath ");
    }

    private static List<List<String>> runtimeClassPath(BuildResult result) {
        return path(result, "-cp ");
    }

    private static List<List<String>> path(BuildResult result, String pathArg) {
        // Parse classpath or module path from Gradle's '--debug' output.
        // The :compileJava and :run tasks log them on that logging level.
        return Arrays.stream(result.getOutput().split("\r?\n|\r")).filter(l -> l.contains(pathArg)).map(l -> {
            int pathArgIndex = l.indexOf(pathArg)  + pathArg.length();
            String pathString = l.substring(pathArgIndex, l.indexOf(" ", pathArgIndex));
            if (pathString.trim().isEmpty()) {
                return Arrays.<String>asList();
            }
            String[] path = pathString.split(System.getProperty("path.separator"));
            return Arrays.stream(path).map(jar -> new File(jar).getName()).sorted().collect(Collectors.toList());
        }).filter(p -> !p.isEmpty()).collect(Collectors.toList());
    }

    private BuildResult build(String task) {
        return build(task, new Properties());
    }

    private BuildResult buildWithJava11(String task) {
        Properties gradleProperties = new Properties();
        gradleProperties.setProperty("org.gradle.java.home", System.getProperty("java11Home"));
        return build(task, gradleProperties);
    }

    private BuildResult build(String task, Properties gradleProperties) {
        try (OutputStream out = new FileOutputStream("test-project/gradle.properties")) {
            gradleProperties.store(out, null);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return GradleRunner.create()
                .withProjectDir(new File("test-project"))
                .withGradleVersion(getGradleVersion())
                .withPluginClasspath()
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp"))
                .withArguments("clean", task, "--stacktrace", "--debug")
                .build();
    }
}
