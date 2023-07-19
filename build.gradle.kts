import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
}

group = "com.proguard.visualizer"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        // jvmToolchain(17)
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                api(compose.material3)
                api(compose.materialIconsExtended)
                implementation("com.darkrockstudios:mpfilepicker:1.2.0")
                implementation("com.google.code.gson:gson:2.10.1")
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
