# android-utilities

## About This Library

This library implements general-purpose functions that can be used from various Android applications. It is used as a common implementation for self-made applications and libraries.

## Installation (Gradle)

In `settings.gradle.kts`, define a reference to the Maven repository at `https://jitpack.io`.

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
    }
}
```

In the module's `build.gradle`, add the dependencies.
```kotlin
dependencies {
    implementation("com.github.toyota-m2k:android-utilities:Tag")
}
```

## Class and Function Descriptions

Please refer to [Reference](reference.md).
