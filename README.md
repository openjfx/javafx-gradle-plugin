# JavaFX Gradle Plugin

Simplifies working with JavaFX 11+ for gradle projects.

## Getting started

To use the plugin, apply the following two steps:

### 1. Apply the plugin

##### Using the `plugins` DSL:

**Groovy**

    plugins {
        id 'org.openjfx.javafxplugin' version '0.0.8'
    }

**Kotlin**

    plugins {
        id("org.openjfx.javafxplugin") version "0.0.8"
    }

##### Alternatively, you can use the `buildscript` DSL:

**Groovy**

    buildscript {
        repositories {
            maven {
                url "https://plugins.gradle.org/m2/"
            }
        }
        dependencies {
            classpath 'org.openjfx:javafx-plugin:0.0.8'
        }
    }
    apply plugin: 'org.openjfx.javafxplugin'

**Kotlin**

    buildscript {
        repositories {
            maven {
                setUrl("https://plugins.gradle.org/m2/")
            }
        }
        dependencies {
            classpath("org.openjfx:javafx-plugin:0.0.8")
        }
    }
    apply(plugin = "org.openjfx.javafxplugin")


### 2. Specify JavaFX modules

Specify all the JavaFX modules that your project uses:

**Groovy**

    javafx {
        modules = [ 'javafx.controls', 'javafx.fxml' ]
    }

**Kotlin**

    javafx {
        modules("javafx.controls", "javafx.fxml")
    }
    
### 3. Specify JavaFX version

To override the default JavaFX version, a version string can be declared.
This will make sure that all the modules belong to this specific version:

**Groovy**

    javafx {
        version = '12'
        modules = [ 'javafx.controls', 'javafx.fxml' ]
    }

**Kotlin**

    javafx {
        version = "12"
        modules("javafx.controls", "javafx.fxml")
    }

### 4. Cross-platform projects and libraries

JavaFX modules require native binaries for each platform. The plugin only
includes binaries for the platform running the build. By declaring the 
dependency configuration **compileOnly**, the native binaries will not be 
included. You will need to provide those separately during deployment for 
each target platform.

**Groovy**

    javafx {
        version = '12'
        modules = [ 'javafx.controls', 'javafx.fxml' ]
        configuration = 'compileOnly'
    }

**Kotlin**

    javafx {
        version = "12"
        modules("javafx.controls", "javafx.fxml")
        configuration = "compileOnly"
    }

### 5. Using a local JavaFX SDK

By default, JavaFX modules are retrieved from Maven Central. 
However, a local JavaFX SDK can be used instead, for instance in the case of 
a custom build of OpenJFX.

Setting a valid path to the local JavaFX SDK will take precedence:

**Groovy**

    javafx {
        sdk = '/path/to/javafx-sdk'
        modules = [ 'javafx.controls', 'javafx.fxml' ]
    }

**Kotlin**

    javafx {
        sdk = "/path/to/javafx-sdk"
        modules("javafx.controls", "javafx.fxml")
    }