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
        // Ktlint already has a task that creates a `pre-commit` hook:
        // https://github.com/JLLeitschuh/ktlint-gradle#main-tasks
        // So we need to append to it.
        // Run `./gradlew check` before every commit:
        from(File(".git/hooks/pre-commit"))
        tasks("check")
    }
    createHooks(true)
}

rootProject.name = "proguard-core-visualizer"
