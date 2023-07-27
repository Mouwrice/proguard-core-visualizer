# Contribution guide

👋 Hi there, and thanks for considering to contribute!
Be sure to check out this guide before you start. For questions you can always post in the [discussions](https://github.com/Mouwrice/proguard-core-visualizer/discussions) tab.
As for communication goes: there are no strict rules, just be nice to each other. And remember that our intentions (as good as they may be) not always come across as we want it to using text. 😉

## Prerequisites

This application is built using [compose multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).
Make sure you have a working java toolchain that is compatible with the project.

## Code style

A standard configuration of [ktlint](https://github.com/pinterest/ktlint) is enforced on every PR on this repository. 
You will get a nice overview on you Pull Request of some code does not adhere to the styling rules.
To avoid having to manually do the formatting there is a [ktlint gradle](https://github.com/JLLeitschuh/ktlint-gradle) plugin that 
creates tasks to apply the formatting and linting for you.
The plugin also has tasks that you can execute to check the formatting or apply the formatting on the staged files on commit
by creating a pre-commit hook. (See: https://github.com/JLLeitschuh/ktlint-gradle#additional-helper-tasks).

I recommend just running `./gradlew addKtlintFormatGitPreCommitHook`, though. Leaving you to focus on the code and never again on the formatting.

<details>
  <summary>IntelliJ Ktlint Plugin</summary>

  For the IntelliJ users I highly recommend installing the ktlint plugin. After which you can configure it to 
  something like this:
  ![image](https://github.com/Mouwrice/proguard-core-visualizer/assets/56763273/9f263cc4-2387-4e65-8923-f4c552512d9b)

</details>

## SemVer and Conventional Commits

The entire workflow of this repository relies on [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
to be able to achieve automatic [semantic versioning](https://semver.org/).
Try to use conventional commits for your commit messages. It is also enforced in the form of a pre-commit hook and on pull requests.
Although you can bypass the pre-commit hook by using the `--no-verify` option. Which can be usefull, but use it sparingly.
Do not worry if you forget this, when creating a pull request you will get a friendly reminder to set your title to the Conventiontal Commits specification,
along with all the possible options.
