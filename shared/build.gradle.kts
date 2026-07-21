import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) props.load(f.inputStream())
}
val devApiBaseUrl = localProps.getProperty("api.base.url", "http://10.0.2.2:3000")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "com.inclinic.app.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform — UI for shared screens (LoginScreen, etc.)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.kotlinx.datetime)
            // Koin
            implementation(libs.koin.core)
            // Multiplatform Settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.coroutines)
            // KotlinX
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            // Decompose
            implementation(libs.decompose)
            implementation(libs.decompose.extensions.compose)
            // Lucide Icons
            implementation(libs.lucide.icons)
            // Coil 3 — KMP image loading (avatars / doctor photos)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            // QRose — KMP QR code rendering (admin 2FA setup)
            implementation(libs.qrose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.koin.test)
            implementation(libs.turbine)
            implementation(libs.multiplatform.settings.test)
            implementation(libs.kotlinx.coroutines.test)
            // Compose Multiplatform UI testing (runComposeUiTest)
            implementation(libs.compose.uiTest)
        }
        androidMain.dependencies {
            // Ktor OkHttp engine
            implementation(libs.ktor.client.okhttp)
            // Koin Android
            implementation(libs.koin.android)
            // DataStore for encrypted token storage
            implementation(libs.androidx.datastore.preferences)
            // Activity Compose — file/image picker (ActivityResultContracts)
            implementation(libs.androidx.activity.compose)
            // GPS de check-in en visitas a domicilio (F4.2)
            implementation(libs.play.services.location)
            implementation(libs.kotlinx.coroutines.play.services)
        }
        iosMain.dependencies {
            // Ktor Darwin engine
            implementation(libs.ktor.client.darwin)
        }
    }
}

buildkonfig {
    packageName = "com.inclinic.app.config"
    exposeObjectWithName = "BuildKonfig"

    // Default points to local emulator — no flavor flag needed for dev.
    // Override via local.properties: api.base.url=http://10.0.2.2:3005
    defaultConfigs {
        buildConfigField(STRING, "API_BASE_URL", devApiBaseUrl)
        buildConfigField(STRING, "ENVIRONMENT", "DEV")
    }

    // Dev flavor — Android emulator: 10.0.2.2 maps to host localhost.
    // Override via local.properties: api.base.url=http://10.0.2.2:3005
    defaultConfigs("dev") {
        buildConfigField(STRING, "API_BASE_URL", devApiBaseUrl)
        buildConfigField(STRING, "ENVIRONMENT", "DEV")
    }

    defaultConfigs("staging") {
        buildConfigField(STRING, "API_BASE_URL", "https://staging.api.clinicai.com")
        buildConfigField(STRING, "ENVIRONMENT", "STAGING")
    }

    defaultConfigs("prod") {
        buildConfigField(STRING, "API_BASE_URL", "https://in-clinic.app")
        buildConfigField(STRING, "ENVIRONMENT", "PROD")
    }
}
