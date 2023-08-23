plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.aelf.portkey"
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
        minSdk = 26

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
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("io.github.oleksandrbalan:modalsheet:0.6.0")
    implementation ("org.jetbrains.kotlin:kotlin-reflect")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")


    implementation("io.aelf:portkey-java-sdk:0.0.7-SNAPSHOT") {
        isChanging = true
        exclude("io.github.billywei01")
        exclude("org.bouncycastle")
        exclude("org.realityforge.org.jetbrains.annotations")
    }
    // https://mvnrepository.com/artifact/io.github.billywei01/fastkv
    implementation("io.github.billywei01:fastkv:2.1.3")
    implementation("com.afollestad.material-dialogs:bottomsheets:3.3.0")
    implementation("io.coil-kt:coil-compose:1.4.0")

    //dependency for the reCAPTCHA (safetynet)
    implementation("com.google.android.gms:play-services-safetynet:18.0.1")
    implementation("com.airbnb.android:lottie:6.1.0")

    // Solve the conflict problem... LOL
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
}