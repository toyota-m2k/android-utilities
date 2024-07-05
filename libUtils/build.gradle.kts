plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = "com.github.toyota-m2k"
version = "1.0"

android {
    namespace = "io.github.toyota32k.utils"
    compileSdk = 34

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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    val lifecycle_ktx_version = "2.8.3"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_ktx_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_ktx_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_ktx_version")
    implementation("androidx.lifecycle:lifecycle-service:$lifecycle_ktx_version")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
}

//afterEvaluate {
//    publishing {
//        publications {
//            create("release", MavenPublication::class) {
//                from(components["release"])
//
//                groupId = "com.github.toyota-m2k"
//                artifactId = "android-utilities"
//                version = "LOCAL"
//            }
//        }
//    }
//}

publishing {
    publications {
        // Creates a Maven publication called "release".
        register<MavenPublication>("release") {
            // You can then customize attributes of the publication as shown below.
            groupId = "com.github.toyota-m2k"
            artifactId = "android-utilities"
            version = "1.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
