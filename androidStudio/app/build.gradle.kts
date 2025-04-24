plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.recipeproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.recipeproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding {
        enable = true
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
}

////image 경로로 띄우기
//repositories {
//    google()
//    mavenCentral()
//}

dependencies {
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.android.volley:volley:1.2.0")

    //이미지
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

//    implementation ("com.github.bumptech.glide:glide:4.12.0") // 예시로 Glide 추가
//    implementation ("com.android.volley:volley:1.2.1")
//    implementation ("com.github.VolleyMultipartRequest:VolleyMultipartRequest:1.0.0")

    implementation("org.tensorflow:tensorflow-lite:2.9.0") //종속성 추가 tensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")

    implementation ("commons-io:commons-io:2.11.0")

    implementation(libs.swiperefreshlayout)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}