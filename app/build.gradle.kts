import java.util.Properties
import java.io.FileInputStream
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.apkupdater"
    compileSdk = 34

    val buildNumber = System.getenv("BUILD_NUMBER").orEmpty()
    defaultConfig {
        applicationId = "com.apkupdater" + System.getenv("BUILD_TAG").orEmpty()
        minSdk = 21
        targetSdk = 34
        versionCode = 52
        versionName = if (buildNumber.isEmpty()) "3.0.3" else "0.0.$buildNumber"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            try {
                val props = Properties()
                props.load(FileInputStream(file("../local.properties")))
                storeFile = file(props.getProperty("keystore.file"))
                storePassword = props.getProperty("keystore.password")
                keyAlias = props.getProperty("keystore.keyalias")
                keyPassword = props.getProperty("keystore.keypassword")
            } catch (ignored: Exception) {
                val config = signingConfigs.getByName("debug")
                storeFile = config.storeFile
                storePassword = config.storePassword
                keyAlias = config.keyAlias
                keyPassword = config.keyPassword
                println("Signing config not found. Using debug settings.")
            }
            enableV3Signing = true
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            val variant = (this as BaseVariantOutputImpl)
            variant.outputFileName = defaultConfig.applicationId + "-" + buildType.name + ".apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions { jvmTarget = "1.8" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.1" }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        warning.addAll(arrayOf("ExtraTranslation", "MissingTranslation", "MissingQuantity"))
    }
}

dependencies {

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")
    implementation("androidx.tv:tv-foundation:1.0.0-alpha10")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.github.rumboalla.KryptoPrefs:kryptoprefs-gson:0.4.3")
    implementation("com.github.rumboalla.KryptoPrefs:kryptoprefs:0.4.3")
    implementation("com.github.topjohnwu.libsu:core:5.2.1")
    implementation("com.aurora:gplayapi:3.2.11")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.github.g00fy2:versioncompare:1.5.0")
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")
    implementation("org.jsoup:jsoup:1.16.1")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.4")

}
