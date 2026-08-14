import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")

    // Firebase / Google Services
    id("com.google.gms.google-services")
}

val restBaseUrl = providers.gradleProperty("REST_BASE_URL")
    .orElse("https://dummyjson.com/")
    .map { if (it.endsWith("/")) it else "$it/" }
    .get()

android {
    namespace = "com.example.apprestaurante"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.apprestaurante"
        minSdk = 24
        targetSdk = 36

        versionCode = 8
        versionName = "5.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            "REST_BASE_URL",
            "\"$restBaseUrl\""
        )

        // Ya sabemos que Firebase está conectado porque
        // google-services.json se encuentra dentro de app/
        buildConfigField(
            "boolean",
            "FIREBASE_CONFIGURED",
            "true"
        )
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/DEPENDENCIES"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    arg(
        "room.schemaLocation",
        "$projectDir/schemas"
    )
}

dependencies {

    // =========================================================
    // JAVA / DESUGARING
    // =========================================================

    coreLibraryDesugaring(
        "com.android.tools:desugar_jdk_libs:2.1.5"
    )

    // =========================================================
    // ANDROID
    // =========================================================

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")

    implementation(
        "com.google.android.material:material:1.13.0"
    )

    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.9.4"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4"
    )

    implementation(
        "androidx.lifecycle:lifecycle-livedata-ktx:2.9.4"
    )

    implementation(
        "androidx.recyclerview:recyclerview:1.4.0"
    )

    implementation(
        "androidx.constraintlayout:constraintlayout:2.2.1"
    )

    implementation(
        "androidx.drawerlayout:drawerlayout:1.2.0"
    )

    implementation(
        "androidx.coordinatorlayout:coordinatorlayout:1.3.0"
    )

    implementation(
        "androidx.swiperefreshlayout:swiperefreshlayout:1.2.0"
    )

    // =========================================================
    // ROOM / SQLITE
    // =========================================================

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")

    ksp(
        "androidx.room:room-compiler:2.8.4"
    )

    // =========================================================
    // COROUTINES
    // =========================================================

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2"
    )

    // =========================================================
    // RETROFIT / REST
    // =========================================================

    implementation(
        "com.squareup.retrofit2:retrofit:3.0.0"
    )

    implementation(
        "com.squareup.retrofit2:converter-gson:3.0.0"
    )

    implementation(
        "com.squareup.okhttp3:logging-interceptor:4.12.0"
    )

    // =========================================================
    // IMÁGENES
    // =========================================================

    implementation(
        "com.github.bumptech.glide:glide:5.0.5"
    )

    // =========================================================
    // FIREBASE
    // =========================================================

    // Firebase BoM administra versiones compatibles
    implementation(
        platform("com.google.firebase:firebase-bom:34.17.0")
    )

    // Firebase Authentication
    implementation(
        "com.google.firebase:firebase-auth"
    )

    // =========================================================
    // GOOGLE SIGN-IN / CREDENTIAL MANAGER
    // =========================================================

    implementation(
        "androidx.credentials:credentials:1.3.0"
    )

    implementation(
        "androidx.credentials:credentials-play-services-auth:1.3.0"
    )

    implementation(
        "com.google.android.libraries.identity.googleid:googleid:1.1.1"
    )

    // =========================================================
    // TESTS
    // =========================================================

    testImplementation(
        "junit:junit:4.13.2"
    )

    testImplementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2"
    )

    androidTestImplementation(
        "androidx.test.ext:junit:1.3.0"
    )

    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.7.0"
    )
}