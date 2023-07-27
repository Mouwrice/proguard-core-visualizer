rootProject.name = "proguard-core-visualizer"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }
}

plugins {
    // Unfortunately, version catalogs can not be used in settings.gradle.kts
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.9"
}

gitHooks {
    commitMsg {
        conventionalCommits()
    }
    createHooks()
}
