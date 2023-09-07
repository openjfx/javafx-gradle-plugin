# JavaFX Gradle Plugin

Simplifies working with JavaFX 11+ for gradle projects.

[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/openjfx/javafxplugin/org.openjfx.javafxplugin.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin)](https://plugins.gradle.org/plugin/org.openjfx.javafxplugin)
![Github Actions](https://github.com/openjfx/javafx-gradle-plugin/actions/workflows/build.yml/badge.svg)
[![BSD-3 license](https://img.shields.io/badge/license-BSD--3-%230778B9.svg)](https://opensource.org/licenses/BSD-3-Clause)

## Getting started

To use the plugin, apply the following two steps:

### 1. Apply the plugin

##### Using the `plugins` DSL:

**Groovy**

    plugins {
        id 'org.openjfx.javafxplugin' version '0.1.0'
    }

**Kotlin**

    plugins {
        id("org.openjfx.javafxplugin") version "0.1.0"
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
            classpath 'org.openjfx:javafx-plugin:0.1.0'
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
            classpath("org.openjfx:javafx-plugin:0.1.0")
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

<pre><code>
javafx {
    <b>version = '17'</b>
    modules = [ 'javafx.controls', 'javafx.fxml' ]
}
</code></pre>

**Kotlin**

<pre><code>
javafx {
    <b>version = "17"</b>
    modules("javafx.controls", "javafx.fxml")
}
</code></pre>

### 4. Cross-platform projects and libraries

The plugin will include JavaFX dependencies for the current platform.
However, a different target platform can also be specified.

Supported targets are:

* linux
* linux-aarch64
* win or windows
* osx or mac or macos
* osx-aarch64 or mac-aarch64 or macos-aarch64 (support added in JavaFX 11.0.12 LTS and JavaFX 17 GA)

**Groovy**

<pre><code>
javafx {
    modules = [ 'javafx.controls', 'javafx.fxml' ]
    <b>platform = 'mac'</b>
}
</code></pre>

**Kotlin**

<pre><code>
javafx {
    modules("javafx.controls", "javafx.fxml")
    <b>platform = 'mac'</b>
}
</code></pre>


### 5. Dependency scope

JavaFX application require native binaries for each platform to run.
By default, the plugin will include these binaries for the target platform.
Native dependencies can be avoided by declaring the dependency configuration as **compileOnly**.

**Groovy**

<pre><code>
javafx {
    modules = [ 'javafx.controls', 'javafx.fxml' ] 
    <b>configuration = 'compileOnly'</b>
}
</code></pre>

**Kotlin**

<pre><code>
javafx {
    modules("javafx.controls", "javafx.fxml")
    <b>configuration = "compileOnly"</b>
}
</code></pre>

Multiple configurations can also be targeted by using `configurations`.
For example, JavaFX dependencies can be added to both `implementation` and `testImplementation`.

**Groovy**

<pre><code>
javafx {
    modules = [ 'javafx.controls', 'javafx.fxml' ]
    <b>configurations = [ 'implementation', 'testImplementation' ]</b>
}
</code></pre>

**Kotlin**

<pre><code>
javafx {
    modules("javafx.controls", "javafx.fxml")
    <b>configurations("implementation", "testImplementation")</b>
}
</code></pre>

### 5. Using a local JavaFX SDK

By default, JavaFX modules are retrieved from Maven Central. 
However, a local JavaFX SDK can be used instead, for instance in the case of 
a custom build of OpenJFX.

Setting a valid path to the local JavaFX SDK will take precedence:

**Groovy**

<pre><code>
javafx {
    <b>sdk = '/path/to/javafx-sdk'</b>
    modules = [ 'javafx.controls', 'javafx.fxml' ]
}
</code></pre>

**Kotlin**

<pre><code>
javafx {
    <b>sdk = "/path/to/javafx-sdk"</b>
    modules("javafx.controls", "javafx.fxml")
}
</code></pre>
    
## Issues and Contributions

Issues can be reported to the [Issue tracker](https://github.com/openjfx/javafx-gradle-plugin/issues/).

Contributions can be submitted via [Pull requests](https://github.com/openjfx/javafx-gradle-plugin/pulls/), 
providing you have signed the [Gluon Individual Contributor License Agreement (CLA)](https://cla.gluonhq.com/).

## Migrating from 0.0.14 to 0.1.0

The previous version rewrote the whole classpath/module path and removed the "wrong" transitive Jars. This is no
longer the case. If you have problems with mixed JavaFX jars (e.g. `javafx-base-linux`, `java-base-mac`) during
`assemble` task or see errors like `Error initializing QuantumRenderer: no suitable pipeline found` it is likely
one or more of your dependencies may have published metadata that includes JavaFX dependencies with classifiers.
The ideal solution is to reach out to library authors to update their JavaFX plugin and publish a patch with fixed
metadata. A fallback solution to this is to `exclude group: 'org.openjfx'` on the dependencies causing the issue.

Additionally on `0.0.14` and below there was a transitive dependency on `org.javamodularity.moduleplugin`.
If your **modular** project stops working after updating to `0.1.0` or above it is likely that you need to
explicitly add the [org.javamodularity.moduleplugin](https://plugins.gradle.org/plugin/org.javamodularity.moduleplugin)
back to your build and set `java.modularity.inferModulePath.set(false)` to keep things working as they were.

Note: There are other recommended alternatives over `org.javamodularity.moduleplugin` for modular projects such as
[extra-java-module-info](https://github.com/gradlex-org/extra-java-module-info) that would allow you to keep
`inferModulePath` set to **true** by declaring missing module information from legacy jars.

**Groovy**

````groovy
plugins {
    // ...
    id 'org.openjfx.javafxplugin' version '0.1.0'
    id 'org.javamodularity.moduleplugin' version '1.8.12'
}

// ...

java {
    // ...
    modularity.inferModulePath.set(false)
}
````

**Kotlin**

````kotlin
plugins {
    // ...
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.javamodularity.moduleplugin") version "1.8.12"
}

// ...

java {
    // ...
    modularity.inferModulePath.set(false)
}
````
