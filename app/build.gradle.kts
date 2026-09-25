plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android { namespace = "com.homiesgym.app"; compileSdk = 36
    defaultConfig { applicationId = "com.homiesgym.app"; minSdk = 26; targetSdk = 36; versionCode = 2; versionName = "2.0.0" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("com.android.billingclient:billing-ktx:9.1.0")
}
