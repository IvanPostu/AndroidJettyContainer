plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.ipostu.androidjettycontainer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ipostu.androidjettycontainer"
        minSdk = 21
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude("META-INF/spring.*")
        exclude("META-INF/hk2-locator/default")
        exclude("META-INF/license.txt")
        exclude("META-INF/*.txt")
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation("org.eclipse.jetty:jetty-webapp:8.1.2.v20120308")
    implementation("org.eclipse.jetty:jetty-deploy:8.1.2.v20120308")
    implementation("org.eclipse.jetty:jetty-client:8.1.2.v20120308")

    implementation("javax.servlet:javax.servlet-api:3.0.1")
    implementation("org.glassfish.jersey.containers:jersey-container-servlet-core:2.26")
    implementation("org.glassfish.jersey.core:jersey-server:2.26")
    implementation("org.glassfish.jersey.inject:jersey-hk2:2.26")
    {
        exclude(group = "javax.inject", module = "javax.inject")
    }
    implementation("javax.xml.stream:stax-api:1.0-2")
    implementation("javax.xml.bind:jaxb-api:2.3.1")



//    implementation("org.glassfish.jersey.ext:jersey-spring4:2.26")
//    {
//        exclude(group = "javax.inject", module = "javax.inject")
//        exclude(group = "org.glassfish.hk2.external", module = "aopalliance-repackaged")
//        exclude(group = "org.glassfish.hk2", module = "hk2-utils")
//
//    }
//    implementation("org.glassfish.hk2:hk2-api:2.26")
//    implementation("org.glassfish.hk2:hk2-locator:2.26")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}