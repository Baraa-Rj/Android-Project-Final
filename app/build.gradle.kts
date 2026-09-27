plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
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

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.splashscreen)

    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // OkHttp for HTTP client and logging
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Gson for JSON serialization
    implementation(libs.gson)

    // Coroutines for async operations
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Lifecycle components (ViewModel, LiveData)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Navigation components
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Volley for HTTP networking (educational contrast with Retrofit)
    implementation("com.android.volley:volley:1.2.1")

    // Unit testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.coroutines.test)

    // Android instrumentation testing dependencies
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}