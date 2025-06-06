plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt") // Enable kapt for annotation processing
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.ecoguard"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ecoguard"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        dataBinding = true // Enable data binding
        viewBinding = true // Enable view binding
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase libraries
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)

    // Android Splash Screen API (optional)
    implementation(libs.androidx.core.splashscreen)

    implementation ("com.squareup.okhttp3:okhttp:4.9.0")

    // RecyclerView
    implementation(libs.androidx.recyclerview)
    // Additional dependencies
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:11.1.0")

    implementation(libs.androidx.scenecore)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.play.services.ads.api)
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation(kotlin("stdlib"))

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
