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

## For maintainers

The following section is for maintainers of the repository and how it is setup. 
It is not needed for regular development but it is here for the keen reader 🤓.

### Ktlint with reviewdog

[Reviewdog](https://github.com/reviewdog/reviewdog) 🐶 will post the ktlint report on any pull request as a PR check. Ktlint check needs to pass before a pull request should be merged.
https://github.com/Mouwrice/proguard-core-visualizer/blob/e3a2e11c71d58c3d308aeedff7b80f3c26239517/.github/workflows/reviewdog.yml#L1-L21

### Semantic pull request title

This is enforced by applying the [semantic-pull-request](https://github.com/marketplace/actions/semantic-pull-request) GitHub Action.

### Automatic releases

Using [Release-Please](https://github.com/google-github-actions/release-please-action#release-please-action) we can automate the creating of releases and changelogs.
More information is provided on their GitHub page.

### Automatic packaging and deployment

Since this is a desktop application, it needs to be packaged into the corresponding format required for the targeted platforms.
[Conveyor](https://conveyor.hydraulic.dev/10.1/) is a tool that automates this process and has been integrated into the workflow by packaging and deploying the application after a release has been published.

The following two properties in the [conveyor.conf](conveyor.conf) file are used to configure the deployment:
```properties
# Where the packages are hosted, packages use this link to check whether updates are available.
site.base-url = "https://mouwrice.github.io/proguard-core-visualizer/"
# The repository url, needs to be set as conveyor is license free for open source projects.
vcs-url = "https://github.com/Mouwrice/proguard-core-visualizer"
```

#### GitHub pages

Note that conveyor can also create releases, but it is not used as Release Please can do it better.
It will upload the packages to the specified github pages link so be sure that github pages is setup to accept GitHub Actions
![image](https://github.com/Mouwrice/proguard-core-visualizer/assets/56763273/4f9199bc-d354-4eef-ba08-7fcd76635485)

A custom domain can be set if preferred.

The environment should also be setup to accept incoming changes from any branch as the conveyor workflow will run on any release tag.
![image](https://github.com/Mouwrice/proguard-core-visualizer/assets/56763273/f7f65300-7164-46fd-b0bc-fcb2fb81e4dd)


### Secrets and variables > Actions

The following secrets are to be set for some of the actions to function properly:

`RELEASE_PLEASE_PAT` which is a personal access token to allow for this action to trigger other actions:
https://github.com/google-github-actions/release-please-action#github-credentials

Release Please is responsible for setting the project version using the conventional commits that have been created on the main branch
and when the PR is merged that Release Please has created a new release will be made.

`CONVEYOR_SIGNING_KEY` is a key used for self-signing the applications. This is not protected by a password.
https://conveyor.hydraulic.dev/10.1/configs/keys-and-certificates/#root-key
