package com.wagner.gitver

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import org.slf4j.LoggerFactory
import org.slf4j.Logger

class GitVersionPlugin implements Plugin<Project> {

  final static Map DEFAULT_CONFIG = [
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

  Logger logger = LoggerFactory.getLogger(GitVersionPlugin)

  @Override
  void apply(Project project) {

    // def config = project.extensions.create("gitver", GitVersionPluginExtension)
    def config = DEFAULT_CONFIG + (project.gradle.ext.find("gitver") ?: [:])

    config.repository = project.file(config.repository ?: project.projectDir).getAbsoluteFile()

    if (config.prefix.matches(/.*\s.*/)) {
      throw new GradleException("'gradle.ext.gitver.prefix' cannot contain whitespace!")
    }

    if (config.automatic) {
      try {
        project.version = gitVersion(config)
      } catch(IOException | NumberFormatException ex) {
        logger.error("GitVer: Error while determining version: ${ex}")
        project.version = config.fallbackVersion
      }
    }

    project.task('gitver') {
      doLast {
        try {
          ext.version = gitVersion(config)
        } catch(IOException | NumberFormatException ex) {
          logger.lifecycle("GitVer: Error while determining version: ${ex}")
          ext.version = config.fallbackVersion
        }
        logger.quiet(ext.version)
      }
    }
  }

  def printConfig(cfg) {
    def attrs = []
    DEFAULT_CONFIG.each { key, val ->
      def cfgVal = cfg.get(key)
      attrs.add("  ${key}: ${cfgVal}${cfgVal == val ? " (default)" : ""}")
    }
    logger.info("GitVer: Configured with\n${attrs.join("\n")}")
  }

  def gitVersion(cfg) {
    printConfig(cfg)
    def version = cfg.fallbackVersion
    def commits = 0
    def revision = "unknown"
    def cmd = "git describe --tags --long --match=${cfg.prefix}* --candidates=50"
    def proc = cmd.execute(null, cfg.repository)
    def stderr = new StringBuffer()
    def stdout = new StringBuffer()
    proc.waitForProcessOutput(stdout, stderr)
    proc.waitForOrKill(10000)
    if (proc.exitValue() == 0) {
      def output = stdout.toString().trim()
      logger.info("GitVer: Received '${output}' from git describe")

      (version, commits, revision) = parseDescribe(output)

      if (cfg.stripPrefix && cfg.prefix) {
        version = version.minus(cfg.prefix)
      }
      if (cfg.includeCommits && commits > 0) {
        version += "${cfg.separatorText}${cfg.commitText}${commits}"
      }
      if (cfg.includeBranch) {
        def branchName = "HEAD"
        def branchCmd = "git rev-parse --abbrev-ref HEAD"
        def branchProc = branchCmd.execute(null, cfg.repository)
        def branchStdout = new StringBuffer(), branchStderr = new StringBuffer()
        branchProc.waitForProcessOutput(branchStdout, branchStderr)
        branchProc.waitForOrKill(10000)
        if(branchProc.exitValue() == 0) {
          branchName = branchStdout.toString().trim()
          logger.info("GitVer: Determined branch to be ${branchName}")
        } else {
          logger.lifecycle("${branchCmd} exited with code ${branchProc.exitValue()}: ${branchStderr.toString().trim()}")
        }

        if (branchName == "HEAD") {
          logger.info("GitVer: HEAD branch detected/fallen back to, using revision as branch name")
          version += "${cfg.separatorText}${revision}"
        } else if (cfg.branchExcludes == null || (!cfg.branchExcludes.equals(branchName) && !cfg.branchExcludes.contains(branchName))) {
          version += "${cfg.separatorText}${branchName}"
        } else {
          logger.info("GitVer: Excluding branch ${branchName}")
        }
      }
      if (cfg.includeSnapshot && cfg.snapshotText && commits > 0) {
        version += "${cfg.separatorText}${cfg.snapshotText}"
      }
    } else {
      // only log to lifecycle to ensure '-q' will only print version
      logger.lifecycle("GitVer: Error while determining version ('${cmd}' exited with code ${proc.exitValue()}): '${stderr.toString().trim()}'")
    }

    logger.info("GitVer: Determined version to be ${version}")
    return version
  }

  def parseDescribe(String output) {
    // with --long, will always print three groups; reversing
    // the output ensures we only split on last two '-'
    def split = output.reverse().split('-', 3)*.reverse()
    split = [split[2], split[1] as int, split[0]]
    logger.debug("GitVer: parseDescribe = ${output} -> ${split}")
    return split
  }
}
