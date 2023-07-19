pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("multiplatform") version "1.8.20"
        id("org.jetbrains.compose") version "1.4.1"
    }
}

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.9"
}

gitHooks {
    commitMsg {
        conventionalCommits()
    }
    // Ktlint already has a task that creates a `pre-commit` hook:
    // https://github.com/JLLeitschuh/ktlint-gradle#main-tasks
    // So we need to manually append to it.
    // Append
    // set -e
    // ./gradlew check
    // set +e
    // to .git/hooks/pre-commit.
    createHooks()
}

rootProject.name = "proguard-core-visualizer"
