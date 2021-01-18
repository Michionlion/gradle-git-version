def gitVersion(prefix = "v", includeSnapshot = true) {
  logger.info("Finding version with prefix='${prefix}' and includeSnapshot=${includeSnapshot}")
  def version = "0.0.0"
  def snapshot = "SNAPSHOT"
  def commits = 0
  def cmd = "git describe --tags --match=${prefix}* --candidates=50"
  def proc = cmd.execute(null, projectDir)
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

ext {
  gitVersion = this.&gitVersion
}
