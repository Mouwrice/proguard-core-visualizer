import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") version "1.8.20"
    id("org.jetbrains.compose") version "1.4.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
}

group = "com.proguard.visualizer"
version = "1.0.0"

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
                implementation("com.darkrockstudios:mpfilepicker:1.2.0")
                implementation("com.google.code.gson:gson:2.10.1")
                implementation("com.materialkolor:material-kolor:1.1.0")
                implementation("com.github.Dansoftowner:jSystemThemeDetector:3.8")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "proguard-core-visualizer"
            packageVersion = "1.0.0"
        }
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("0.50.0")
    verbose.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(true)
}
