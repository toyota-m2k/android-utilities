import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

group = "com.github.toyota-m2k"
version = "1.0"

kotlin {
    jvmToolchain(21)
}

configure<LibraryExtension> {
    namespace = "io.github.toyota32k.utils"
    compileSdk = 36

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    packaging.resources.excludes.add("META-INF/DEPENDENCIES")
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
    implementation(libs.gson)
    api(libs.android.logger)
    implementation(libs.androidx.documentfile)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junitExt)
    androidTestImplementation(libs.espressoCore)

    testImplementation(libs.androidx.junit.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.core)
    testImplementation(libs.core.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.robolectric)
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
