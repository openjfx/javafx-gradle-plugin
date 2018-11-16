# JavaFX Gradle Plugin

Simplifies working with JavaFX 11+ for gradle projects.

## Getting started

### Modular projects

For modular projects, you only need to apply the plugin. The plugin will automatically add the required JavaFX dependencies based on the module definition.

    plugins {
        id 'java'
        id 'org.openjfx.javafxplugin' version '0.0.1'
    }

### Non-modular projects

For non-modular projects, you need to apply the plugin and define the list of JavaFX modules required by the project.

    plugins {
        id 'java'
        id 'org.openjfx.javafxplugin' version '0.0.1'
    }
    
    javafx {
        modules = [ 'javafx.controls' ]
    }
