# JavaFX Gradle Plugin

Simplifies working with JavaFX 11+ for gradle projects.

## Getting started

To use the plugin, apply the following two steps:

### 1. Apply the plugin

Using the `plugins` DSL:

    plugins {
        id 'org.openjfx.javafxplugin' version '0.0.6'
    }

Alternatively, you can use the `buildscript` DSL:

    buildscript {
        repositories {
            maven {
                url "https://plugins.gradle.org/m2/"
            }
        }
        dependencies {
            classpath 'org.openjfx:javafx-plugin:0.0.6'
        }
    }

    apply plugin: 'org.openjfx.javafxplugin'

### 2. Specify JavaFX modules

Specify all the JavaFX modules that your project uses:

    javafx {
        modules = [ 'javafx.controls', 'javafx.fxml' ]
    }
