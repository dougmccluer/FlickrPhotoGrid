import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.android.build.api.variant.BuildConfigField

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}

//inject the API key from local.properties so that it's not checked into source control.
// For this small project that's probably enough, but it's worth noting that the key will still be
// embedded in the generated BuildConfig class and easy to obtain by decompiling the apk. If we
// wanted extra protection, we might consider delivering the API key at runtime and using cert
// pinning and device attestation to make it more difficult to extract
val flickrApiKey: String = gradleLocalProperties(rootDir,providers).getProperty("FLICKR_API_KEY")

android {
    buildFeatures{
        buildConfig = true
    }
    namespace = "com.example.fauxtoes"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.fauxtoes"
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
    kotlin{
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    buildFeatures {
        compose = true
    }
}

androidComponents {
    onVariants {
        it.buildConfigFields?.put("FLICKR_API_KEY",
            BuildConfigField("String",
            '"'+flickrApiKey+'"',
            "API key for flickr service"
        ) )
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":design_system"))
    implementation(project(":flickr_service"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.runtime)

    // Koin for Android
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.annotations)
    implementation(libs.navigation.compose)
    ksp(libs.koin.ksp.compiler)

    implementation(libs.coil)
    implementation(libs.coil.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)

    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)
    implementation(libs.okHttp.loggingInterceptor)

    implementation(libs.material.icons.core)

    implementation(libs.timber)

    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(kotlin("test"))
}