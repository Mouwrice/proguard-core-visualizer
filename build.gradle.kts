version = "0.1.0"

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.multiplatform
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jlleitschuh.ktlint)
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}

kotlin {
    jvm {
    }
    sourceSets {

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                api(compose.material3)
                api(compose.materialIconsExtended)
                implementation(libs.darkrockstudios.mpfilepicker)
                implementation(libs.google.gson)
                implementation(libs.materialkolor)
                implementation(libs.dansoftowner.jthemedetecor)
            }
        }
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set(libs.versions.ktlint)
    verbose.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(true)
}
