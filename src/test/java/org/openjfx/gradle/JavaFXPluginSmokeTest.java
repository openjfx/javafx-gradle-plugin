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

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class JavaFXPluginSmokeTest {

    private static final String classifier;

    static {
        var p = ProjectBuilder.builder().build();
        p.getPlugins().apply(JavaFXPlugin.class);
        classifier = p.getExtensions().getByType(JavaFXOptions.class).getPlatform().getClassifier();
    }

    protected abstract String getGradleVersion();

    protected String modularApplicationRuntime() {
        return "modular-with-modularity-plugin.jar";
    }

    @Test
    void smokeTestModular() {
        var result = build(":modular:run");

        assertEquals(TaskOutcome.SUCCESS, result.task(":modular:run").getOutcome());

        assertEquals(List.of("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar"), modulePath(result).get(0));
        assertEquals(List.of("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar", "modular.jar"), modulePath(result).get(1));

        assertEquals(List.of(), compileClassPath(result));
        assertEquals(List.of(), runtimeClassPath(result));
    }

    @Test
    void smokeTestModularWithModularityPlugin() {
        Assumptions.assumeFalse(useConfigurationCache(), "modularity plugin does not support configuration cache");
        var result = build(":modular-with-modularity-plugin:run");

        assertEquals(TaskOutcome.SUCCESS, result.task(":modular-with-modularity-plugin:run").getOutcome());

        assertEquals(List.of("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar"), modulePath(result).get(0));
        assertEquals(List.of("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar", modularApplicationRuntime()), modulePath(result).get(1));

        assertEquals(List.of(), compileClassPath(result));
        assertEquals(List.of(), runtimeClassPath(result));
    }

    @Test
    void smokeTestNonModular() {
        var result = build(":non-modular:run");

        assertEquals(TaskOutcome.SUCCESS, result.task(":non-modular:run").getOutcome());

        assertEquals(List.of("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar", "javafx-media-17-" + classifier + ".jar", "javafx-web-17-" + classifier + ".jar"), compileClassPath(result).get(0));
        assertEquals(List.of("main", "main"), runtimeClassPath(result).get(0));
        assertEquals(List.of("javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar", "javafx-media-17-" + classifier + ".jar", "javafx-web-17-" + classifier + ".jar"), modulePath(result).get(0));
    }

    @Test
    void smokeTestTransitive() {
        var result = build(":transitive:run");

        assertEquals(TaskOutcome.SUCCESS, result.task(":transitive:run").getOutcome());

        assertEquals(List.of("charts-17.1.41.jar", "javafx-base-17-" + classifier + ".jar", "javafx-controls-17-" + classifier + ".jar", "javafx-graphics-17-" + classifier + ".jar"), compileClassPath(result).get(0));
        assertEquals(List.of("charts-17.1.41.jar", "countries-17.0.29.jar", "heatmap-17.0.17.jar", "logback-classic-1.2.6.jar", "logback-core-1.2.6.jar", "main", "main", "slf4j-api-1.7.32.jar", "toolbox-17.0.45.jar", "toolboxfx-17.0.37.jar"), runtimeClassPath(result).get(0));
        assertEquals(List.of("javafx-base-17.0.6-" + classifier + ".jar", "javafx-controls-17.0.6-" + classifier + ".jar", "javafx-graphics-17.0.6-" + classifier + ".jar", "javafx-swing-17.0.6-" + classifier + ".jar"), modulePath(result).get(0));
    }

    @Test
    void smokeTestLocalSdk() {
        var result = build(":local-sdk:build"); // do not ':run', as it won't run on any platform

        assertEquals(TaskOutcome.SUCCESS, result.task(":local-sdk:build").getOutcome());

        assertEquals(List.of("javafx.base.jar", "javafx.controls.jar", "javafx.graphics.jar"), compileClassPath(result).get(0));
        assertEquals(List.of(), modulePath(result));
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
        return result.getOutput().lines().filter(l -> l.contains(pathArg)).map(l -> {
            int pathArgIndex = l.indexOf(pathArg)  + pathArg.length();
            String pathString = l.substring(pathArgIndex, l.indexOf(" ", pathArgIndex));
            //recently gradle added an empty classpath instead of omitting it entirely which seems like the same thing?
            if (pathString.isBlank() || pathString.equals("\"\"")) {
                return List.<String>of();
            }
            String[] path = pathString.split(System.getProperty("path.separator"));
            return Arrays.stream(path).map(jar -> new File(jar).getName()).sorted().collect(Collectors.toList());
        }).filter(p -> !p.isEmpty()).collect(Collectors.toList());
    }

    private BuildResult build(String task) {
        return GradleRunner.create()
                .withProjectDir(new File("test-project"))
                .withGradleVersion(getGradleVersion())
                .withPluginClasspath()
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp"))
                .withArguments(getGradleRunnerArguments(task))
                .build();
    }

    protected List<String> getGradleRunnerArguments(String taskname) {
        //note that the tests are written around --debug, they will fail if you reduce logging
        final var args = new ArrayList<String>(List.of("clean", taskname, "--stacktrace", "--debug"));
        if (useConfigurationCache()) {
            args.add("--configuration-cache");
        }
        return args;

    }

    protected boolean useConfigurationCache() {
        return false;
    }
}
