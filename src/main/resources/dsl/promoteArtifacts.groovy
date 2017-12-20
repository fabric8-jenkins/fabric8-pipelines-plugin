package dsl

import org.jenkinsci.plugins.fabric8.steps.PromoteArtifacts

def call(PromoteArtifacts.Arguments config) {
  def flow = new Fabric8Commands()
  def name = config.project
  def version = config.version
  def repoIds = config.repoIds
  def containerName = config.containerName

  return flow.doStepExecution(config.stepExtension) {
    if (repoIds && repoIds.size() > 0) {
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

        if (version && config.updateNextDevelopmentVersion) {
          flow.updateNextDevelopmentVersion(version, config.updateNextDevelopmentVersionArguments ?: "")
          return flow.createPullRequest("[CD] Release ${version}", "${config.project}", "release-v${version}")
        }
      }
    }
  }
}
