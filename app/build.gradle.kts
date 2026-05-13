plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.sadaguessgame"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sadawallpaperapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                test.jvmArgs(
                    "-Xmx1g",
                    "--add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
                    "--add-opens=java.base/java.lang=ALL-UNNAMED",
                    "--add-opens=java.base/java.util=ALL-UNNAMED"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX and Material
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Gson
    implementation("com.google.code.gson:gson:2.13.2")

    // Glide
    implementation("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")

    // Lottie
    implementation("com.airbnb.android:lottie:6.7.1")

    // AmbilWarna color picker
    implementation("com.github.yukuku:ambilwarna:2.0.1")

    implementation ("com.google.android.gms:play-services-ads:23.1.0")

    // Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("org.mockito:mockito-core:5.6.0")

    // Instrumented Tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // ── Feature 4 & 6: Firebase (required for FCM push + optional Analytics) ─
    // Add google-services.json first, then uncomment:
    // implementation platform('com.google.firebase:firebase-bom:33.0.0')
    // implementation 'com.google.firebase:firebase-messaging'
    // implementation 'com.google.firebase:firebase-analytics'   // optional

    // ── Feature 1: AmbilWarna colour picker (already used in ScoreBoardActivity) ──
    // Already in your project. No change needed.

    // ── Feature 6: WorkManager (optional — for scheduled "Did you know?" notifs) ──
    // implementation 'androidx.work:work-runtime:2.9.0'
}