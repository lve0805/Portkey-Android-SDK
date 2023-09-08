plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

configurations.all { // check for updates every build
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}




android {
    namespace = "io.aelf.portkey"
    compileSdk = 34

    signingConfigs {

        getByName("debug") {
            storeFile = file("../test.keystore")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
        }
    }

    packagingOptions.resources.excludes.add("META-INF/DEPENDENCIES")
    packagingOptions.resources.excludes.add("META-INF/LICENSE")
    packagingOptions.resources.excludes.add("META-INF/LICENSE.txt")
    packagingOptions.resources.excludes.add("META-INF/license.txt")
    packagingOptions.resources.excludes.add("META-INF/NOTICE")
    packagingOptions.resources.excludes.add("META-INF/NOTICE.txt")
    packagingOptions.resources.excludes.add("META-INF/notice.txt")
    packagingOptions.resources.excludes.add("META-INF/ASL2.0")
    packagingOptions.resources.excludes.add("META-INF/*.kotlin_module")

    configurations {
           all {
               exclude("bcprov-jdk15on")
           }
    }

    defaultConfig {
        applicationId = "io.aelf.portkey"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86_64")
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation(project(mapOf("path" to ":portkey")))
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))

    //Portkey's core Java SDK
    implementation("io.aelf:portkey-java-sdk:0.0.7-SNAPSHOT") {
        isChanging = true
        // Portkey's Java SDK uses FastKV dependency with the same name as the one used by the Android app, so it needs to be excluded.
        exclude("io.github.billywei01")
    }

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}