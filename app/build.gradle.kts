import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
localProperties.load(FileInputStream(localPropertiesFile))

android {
    signingConfigs {
        create("release") {
            keyAlias = localProperties["keyAlias"] as String
            keyPassword = localProperties["keyPassword"] as String
            storeFile = file(localProperties["storeFile"] as String)
            storePassword = localProperties["storePassword"] as String
        }
    }
    namespace = "com.yogiissugeo.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yogiissugeo.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 8
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //API키 값 로컬에서 가져옴
        buildConfigField("String", "CLOTHING_BIN_API_KEY", getLocalProperty("CLOTHING_BIN_API_KEY"))
        buildConfigField("String", "NAVER_MAP_CLIENT_ID", getLocalProperty("NAVER_MAP_CLIENT_ID"))
        buildConfigField("String", "NAVER_MAP_API_KEY", getLocalProperty("NAVER_MAP_API_KEY"))
        buildConfigField("String", "ADMOB_BANNER_AD", getLocalProperty("ADMOB_BANNER_AD"))
    }

    buildTypes {
        release {
            // 코드 난독화 및 최적화 활성화
            isMinifyEnabled = true
            // 사용하지 않는 리소스 제거
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-retrofit2.pro",
                "proguard-gson.pro"
            )
            signingConfig = signingConfigs.named("release").get()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text.google.fonts)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // RxJava
    implementation(libs.rxjava3)
    implementation(libs.rxandroid)
    implementation(libs.retrofit.adapter.rxjava3)

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    // Hilt Navigation Compose
    implementation(libs.androidx.hilt.navigation.compose)

    // Coroutines
    implementation(libs.coroutines)

    //네이버 지도 SDK
    implementation(libs.naver.map)

    //구글 위치 서비스
    implementation(libs.play.service.location)

    //Room
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    kapt(libs.androidx.room.compiler)

    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.config)

    //openCSV
    implementation (libs.opencsv)

    implementation(libs.kotlinx.serialization.json)

    //admob
    implementation(libs.play.services.ads)

    // Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Coil
    implementation(libs.coil.compose)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

fun getLocalProperty(propertyName: String): String {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }
    return properties.getProperty(propertyName, "")
}