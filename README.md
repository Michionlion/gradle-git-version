# gradle-git-version
Get a version string from your Git history in Gradle; by default the `project.version` property is automatically set to the generated version.

Configuration is possible by setting the `gradle.ext.gitver` extension object, probably in `settings.gradle`. The example below shows the default configuration.

```groovy
gradle.ext.gitver = [
  prefix: "v",
  stripPrefix: true,
  includeCommits: true,
  commitText: "b",
  includeSnapshot: true,
  snapshotText: "SNAPSHOT",
  separatorText: "-",
  fallbackVersion: "unspecified",
  automatic: true,
  repository: null
]
```


