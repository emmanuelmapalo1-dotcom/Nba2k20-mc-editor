plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.moddingtools.nba2kmceditor"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.moddingtools.nba2kmceditor"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.10.0")
}
