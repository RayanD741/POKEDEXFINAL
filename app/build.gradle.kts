plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pokedex_final"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pokedex_final"
        minSdk = 24
        targetSdk = 36
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Versions via TOML
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // RecyclerView (nécessaire pour RecyclerView + GridLayoutManager)
    implementation("androidx.recyclerview:recyclerview:1.3.2") // ou + récent

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")         // 2.9.0 fonctionne aussi
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Images
    implementation("com.squareup.picasso:picasso:2.71828")

    // SQLite helper
    implementation("androidx.sqlite:sqlite:2.3.1")

    // Fragments (si tu en as besoin)
    implementation("androidx.fragment:fragment:1.6.2")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

