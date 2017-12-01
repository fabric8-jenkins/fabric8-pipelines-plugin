package dsl

def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def flow = new Fabric8Commands()
  def name = config.projectStagingDetails[0]
  def version = config.projectStagingDetails[1]
  def repoIds = config.projectStagingDetails[2]
  def containerName = config.containerName ?: 'maven'

  container(name: containerName) {
    sh 'chmod 600 /root/.ssh-git/ssh-key'
    sh 'chmod 600 /root/.ssh-git/ssh-key.pub'
    sh 'chmod 700 /root/.ssh-git'

    echo "About to release ${name} repo ids ${repoIds}"
    for (int j = 0; j < repoIds.size(); j++) {
      flow.releaseSonartypeRepo(repoIds[j])
    }

    if (config.helmPush) {
      flow.helm()
    }

    if (!config.useGitTagForNextVersion) {
      flow.updateNextDevelopmentVersion(version, config.setVersionExtraArgs ?: "")
      return flow.createPullRequest("[CD] Release ${version}", "${config.project}", "release-v${version}")
    }
  }
}
