plugins {
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.android.library)
    id("maven-publish")
}

group = "com.github.toyota-m2k"
version = "1.0"

android {
    namespace = "io.github.toyota32k.utils"
    compileSdk = 35

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    packagingOptions.resources.excludes.add("META-INF/DEPENDENCIES")
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.extensions)
    implementation(libs.preference.ktx)

    implementation(libs.lifecycleRuntimeKtx)
    implementation(libs.lifecycleLivedataKtx)
    implementation(libs.lifecycleViewmodelKtx)
    implementation(libs.lifecycleService)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junitExt)
    androidTestImplementation(libs.espressoCore)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.robolectric)
    testImplementation(libs.core.testing)
}

// ./gradlew publishToMavenLocal
publishing {
    publications {
        // Creates a Maven publication called "release".
        register<MavenPublication>("release") {
            // You can then customize attributes of the publication as shown below.
            groupId = "com.github.toyota-m2k"
            artifactId = "android-utilities"
            version = project.findProperty("githubReleaseTag") as String? ?: "LOCAL"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/toyota-m2k/android-utilities")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
