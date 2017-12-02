package dsl

import org.jenkinsci.plugins.fabric8.steps.WaitUntilJenkinsPluginSynced

def call(WaitUntilJenkinsPluginSynced.Arguments config) {
  def flow = new Fabric8Commands()

  def repo = config.repo
  if (!repo) {
    repo = "http://archives.jenkins-ci.org/"
  }
  def name = config.name
  def path = "plugins/" + name
  def artifact = "${name}.hpi"

  waitUntil {
    retry(3) {
      flow.isFileAvailableInRepo(repo, path, config.version, artifact)
    }
  }

  flow.sendChat "${config.artifactId} ${config.version} released and available in the jenkins plugin archive"
}
