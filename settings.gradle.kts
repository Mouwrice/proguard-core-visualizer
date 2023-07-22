rootProject.name = "proguard-core-visualizer"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    // Unnfortunately, version catalogs are not supported in settings.gradle.kts
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.9"
    id("com.mooltiverse.oss.nyx") version "2.0.0"
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

configure<com.mooltiverse.oss.nyx.gradle.NyxExtension> {
    // https://mooltiverse.github.io/nyx/guide/user/configuration-reference/changelog/
    changelog {
        path = "CHANGELOG.md"
        sections.set(
            mutableMapOf(
                Pair(
                    "Added",
                    "^(feat|chore\\(deps\\))$",
                ),
                Pair(
                    "Fixed",
                    "^fix$",
                ),
            ),
        )
    }
    preset.set("simple")
}
