package org.openjfx.gradle;

import com.google.gradle.osdetector.OsDetector;
import org.gradle.api.Project;

public enum JavaFXPlatform {

    LINUX("linux"),
    WINDOWS("win"),
    OSX("mac");

    private String classifier;

    JavaFXPlatform( String classifier ) {
        this.classifier = classifier;
    }

    public String getClassifier() {
        return classifier;
    }

    public static JavaFXPlatform detect(Project project) {
        String os = project.getExtensions().getByType(OsDetector.class).getOs();
        switch (os) {
            case "osx"    : return JavaFXPlatform.OSX;
            case "windows": return JavaFXPlatform.WINDOWS;
            default       : return JavaFXPlatform.LINUX;
        }
    }
}
