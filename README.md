# gradle-git-version

Get a version string from your Git history in Gradle; by default the `project.version` property is automatically set to the generated version. View the version by running `gradle gitver`; print it on every run by adding `logger.lifecycle("\n\u001B[1;36mVERSION ${version}\u001B[0m\n")` to your `build.gradle`. Access it in your build scripts just as normal -- `project.version` is accessible in `build.gradle` with just `version`. Print just the version for use outside of gradle with `gradle gitver --quiet --console=plain`

Configuration is possible by setting the `gradle.ext.gitver` extension object, probably in `settings.gradle`. The example below shows the default configuration.

```groovy
gradle.ext.gitver = [
  prefix: "v",
  stripPrefix: true,
  includeCommits: true,
  commitText: "b",
  separatorText: "-",
  includeSnapshot: true,
  snapshotText: "SNAPSHOT",
  includeBranch: true,
  branchExcludes: ["main","master"],
  fallbackVersion: "unspecified",
  automatic: true,
  repository: null
]
```

This configuration generates versions like `0.1.0` or `0.1.0-b1-defaults-update-SNAPSHOT` -- the first when the latest commit has a tag with the form `v0.1.0`, the second when the current commit is one commit ahead of that release tag. When no release tags are found, the `fallbackVersion` (`unspecified`) is used. You can override the folder used as the current working directory when running `git describe` with the `repository` key, which defaults (when `null`) to the current project directory.

Apply to your project with the below addition to your `plugins` block in `build.gradle`.

```groovy
plugins {
  id "com.wagner.gitver" version "0.2.0"
}
```
