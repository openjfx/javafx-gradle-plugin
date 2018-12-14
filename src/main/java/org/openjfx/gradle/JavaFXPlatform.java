package org.openjfx.gradle;

import com.google.gradle.osdetector.OsDetector;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum JavaFXPlatform {

    LINUX("linux", "linux"),
    WINDOWS("win", "windows"),
    OSX("mac", "osx");

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

        String os = project.getExtensions().getByType(OsDetector.class).getOs();

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
