package com.michionlion.gitver

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.LoggerFactory
import org.slf4j.Logger

class GitVersionPluginExtension {
  String prefix = "v"
  boolean includeSnapshot = true
  File repository = null
}

class GitVersionPlugin implements Plugin<Project> {

  Logger logger = LoggerFactory.getLogger(GitVersionPlugin)

  @Override
  void apply(Project project) {

    def config = project.extensions.create('gitver', GitVersionPluginExtension)

    if (!config.repository) {
      config.repository = project.projectDir
    }

    project.version = gitVersion(config.prefix, config.includeSnapshot, config.repository)

    project.task('gitver') { doLast { logger.quiet(project.version) } }
    project.task('version') { doLast { logger.quiet(project.version) } }
  }

  def gitVersion(String prefix, boolean includeSnapshot, File projDir) {
    logger.info("Finding version with prefix='${prefix}' and includeSnapshot=${includeSnapshot}")
    def version = "0.0.0"
    def snapshot = "SNAPSHOT"
    def commits = 0
    def cmd = "git describe --tags --match=${prefix}* --candidates=50"
    def proc = cmd.execute(null, projDir)
    def stderr = new StringBuffer()
    proc.consumeProcessErrorStream(stderr)
    def exitVal = proc.waitFor()
    if (exitVal == 0) {
      def output = proc.text.trim()
      logger.info("GitVersion: git describe returned '${output}'")
      def split = output.split('-', 2)
      version = split[0].minus(prefix)
      if (split.length > 1) {
        commits = split[1].split('-')[0] as int
      } else {
        snapshot=""
      }
    } else {
      logger.error("GitVersion: Error while determining version (${exitVal}): '${stderr.toString().trim()}'")
    }

    if (commits > 0) {
      version += "-b${commits}"
    }
    if (includeSnapshot && snapshot) {
      version += "-${snapshot}"
    }

    logger.info("GitVersion: Determined version to be ${version}")
    return version
  }
}
