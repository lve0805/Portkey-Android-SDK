plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.aelf.portkey.sdk"
    compileSdk = 34

    packagingOptions.resources.excludes.add("META-INF/DEPENDENCIES")
    packagingOptions.resources.excludes.add("META-INF/LICENSE")
    packagingOptions.resources.excludes.add("META-INF/LICENSE.txt")
    packagingOptions.resources.excludes.add("META-INF/license.txt")
    packagingOptions.resources.excludes.add("META-INF/NOTICE")
    packagingOptions.resources.excludes.add("META-INF/NOTICE.txt")
    packagingOptions.resources.excludes.add("META-INF/notice.txt")
    packagingOptions.resources.excludes.add("META-INF/ASL2.0")
    packagingOptions.resources.excludes.add("META-INF/*.kotlin_module")

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("io.coil-kt:coil-compose:2.4.0")

    //Portkey's core Java SDK
    implementation("io.aelf:portkey-java-sdk:0.0.7-SNAPSHOT") {
        isChanging = true
        // Portkey's Java SDK uses FastKV dependency with the same name as the one used by the Android app, so it needs to be excluded.
        exclude("io.github.billywei01")
        // org.bouncycastle's dependency will conflict with each other
        exclude("org.bouncycastle")
        exclude("org.realityforge.org.jetbrains.annotations")
    }

    implementation("org.bouncycastle:bcprov-jdk15on:1.69")

    // https://mvnrepository.com/artifact/io.github.billywei01/fastkv
    implementation("io.github.billywei01:fastkv:2.3.0")
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Google's dependency
    implementation("com.google.android.gms:play-services-safetynet:18.0.1")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    implementation("com.lightspark:compose-qr-code:1.0.1")

    // Solve the conflict problem... LOL
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
}