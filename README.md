# JavaFX Gradle Plugin

Simplifies working with JavaFX 11+ for gradle projects.

## Getting started

### Apply the plugin

Using the `plugins` DSL:

    plugins {
        id 'java'
        id 'org.openjfx.javafxplugin' version '0.0.3'
    }

Alternatively, you can use the `buildscript` DSL:

    buildscript {
        repositories {
            maven {
                url "https://plugins.gradle.org/m2/"
            }
        }
        dependencies {
            classpath 'org.openjfx:javafx-plugin:0.0.3'
        }
    }

    apply plugin: 'java'
    apply plugin: 'org.openjfx.javafxplugin'

### Specify JavaFX modules

Specify all the JavaFX modules that your project uses:

    javafx {
        modules = [ 'javafx.controls', 'javafx.fxml' ]
    }
