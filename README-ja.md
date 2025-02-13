# android-utilities

## このライブラリについて

さまざまなAndroidアプリから利用できる汎用的な機能を実装したライブラリです。
自作のアプリ、ライブラリの共通実装として利用しています。

## インストール (Gradle)

settings.gradle.kts で、mavenリポジトリ https://jitpack.io への参照を定義します。  

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

モジュールの build.gradle で、dependencies を追加します。
```kotlin
dependencies {
    implementation("com.github.toyota-m2k:android-utilities:Tag")
}
```

## クラス・関数の説明

[Reference](reference-ja.md) をご参照ください。
