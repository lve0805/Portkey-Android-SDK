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
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.lightspark:compose-qr-code:1.0.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("androidx.biometric:biometric:1.2.0-alpha05")


    implementation("io.aelf:portkey-java-sdk:0.0.7-SNAPSHOT") {
        isChanging = true
        exclude("io.github.billywei01")
        exclude("org.bouncycastle")
        exclude("org.realityforge.org.jetbrains.annotations")
    }
    // https://mvnrepository.com/artifact/io.github.billywei01/fastkv
    implementation("io.github.billywei01:fastkv:2.1.3")
    implementation("com.jraska:console:1.2.0")
    implementation("com.afollestad.material-dialogs:bottomsheets:3.3.0")
    implementation("com.jraska:console-timber-tree:1.2.0")
    //dependency for the reCAPTCHA (safetynet)
    implementation("com.google.android.gms:play-services-safetynet:18.0.1")
    implementation("com.afollestad.material-dialogs:input:3.3.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Solve the conflict problem... LOL
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

}