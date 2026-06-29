plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
}

// Force kotlinx-datetime to 0.7.1 across all subprojects so the iOS and Android
// targets resolve the same artifact version. Compose Multiplatform 1.9+ requires
// 0.7.1; without this the JVM target silently stays on 0.6.2, causing type
// mismatches between kotlin.time.Instant (0.7.x) and kotlinx.datetime.Instant (0.6.x).
subprojects {
    configurations.configureEach {
        resolutionStrategy {
            force("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        }
    }
}