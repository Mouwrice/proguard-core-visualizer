rootProject.name = "proguard-core-visualizer"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    // Unfortunately, version catalogs are not supported in settings.gradle.kts
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.9"
}

gitHooks {
    commitMsg {
        conventionalCommits()
    }
    createHooks()
}

includeBuild("/home/jitse/Documents/core") {
    name = "proguard-core"
}