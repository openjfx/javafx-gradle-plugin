package org.openjfx.gradle;

import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaFXPluginSmokeTest {

    @Test
    void smokeTest() {
        var result = GradleRunner.create()
                .withProjectDir(new File("test-project"))
                .withGradleVersion("5.0")
                .withArguments("clean", "build", "run", "--stacktrace")
                .forwardOutput()
                .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":modular:run").getOutcome(), "Failed build!");
        assertEquals(TaskOutcome.SUCCESS, result.task(":non-modular:run").getOutcome(), "Failed build!");
    }
}
