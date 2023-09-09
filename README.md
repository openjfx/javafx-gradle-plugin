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

Version `0.1.0` had a couple number of changes and improvements such as lazy dependency declaration,
variant-aware dependency management, and support for Gradle's built-in JPMS functionality. In addition,
the previous version used to rewrite the classpath/module path. This is no longer the case to minimize
making changes to your build and as a result your past builds may be potentially affected when you upgrade
the plugin. On the next section there's a couple of troubleshooting steps that may help with the transition
if you encounter issues when upgrading. These examples are provided in a best-effort basis, but feel free to
open an issue if you think there's a migration scenario that's not captured here that should be included.

### Troubleshooting

#### Mixed JavaFX jars

If you see mixed JavaFX jars (e.g. `javafx-base-x.y.z-linux.jar`, `java-base-x.y.z-mac.jar`) during `assemble`
task or see errors like `Error initializing QuantumRenderer: no suitable pipeline found` it is likely one or more
of your dependencies may have published metadata that includes JavaFX dependencies with classifiers. The ideal
solution is to reach out to library authors to update their JavaFX plugin and publish a patch with fixed metadata.
A fallback solution to this is to `exclude group: 'org.openjfx'` on the dependencies causing the issue.

#### Variants

If you see errors such as `Cannot choose between the following variants of org.openjfx...` it is possible that
your build or another plugin is interacting with the classpath/module path in a way that "breaks" functionality
provided by this plugin. In such cases, you may need to re-declare the variants yourself as described in
[Gradle docs on attribute matching/variants](https://docs.gradle.org/current/userguide/variant_attributes.html)
or reach out to the plugin author in an attempt to remediate the situation. Below is an example where
`org.unbroken-dome.xjc` plugin was doing exactly this and two approaches that can be taken to resolve it.

```groovy
// Approach 1: Explicit Variant
configurations.xjcCatalogResolution  {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
        attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(OperatingSystemFamily, "linux"))
        attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, "x86-64"))
    }
}

// Approach 2: Copy existing configuration over to xjcCatalogResolution
configurations.xjcCatalogResolution  {
    def rtCpAttributes = configurations.runtimeClasspath.attributes
    rtCpAttributes.keySet().each { key ->
        attributes.attribute(key, rtCpAttributes.getAttribute(key))
    }
}
```

#### Extra plugins

On `0.0.14` and below there was a transitive dependency on `org.javamodularity.moduleplugin`.
If your **modular** project stops working after updating to `0.1.0` or above it is likely that you need to
explicitly add the [org.javamodularity.moduleplugin](https://plugins.gradle.org/plugin/org.javamodularity.moduleplugin)
back to your build and set `java.modularity.inferModulePath.set(false)` to keep things working as they were.
This change should not be required for non-modular projects.

**Note**: There are other recommended alternatives over `org.javamodularity.moduleplugin` for modular projects such as
[extra-java-module-info](https://github.com/gradlex-org/extra-java-module-info) that would allow you to keep
`inferModulePath` set to **true** by declaring missing module information from legacy jars. More details on how to
accomplish can be found on the plugin's source code repository.

**Before**

````groovy
plugins {
    id 'org.openjfx.javafxplugin' version '0.0.14'
}
````

**After**

````groovy
plugins {
    id 'org.openjfx.javafxplugin' version '0.1.0'
    id 'org.javamodularity.moduleplugin' version '1.8.12'
}

java {
    modularity.inferModulePath.set(false)
}
````
