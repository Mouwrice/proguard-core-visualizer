pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.9"


}

gitHooks {
    commitMsg {
        conventionalCommits()
    }
    preCommit {
        tasks("check", requireSuccess = true)
    }
    createHooks(true)
}


rootProject.name = "proguard-core-visualizer"
