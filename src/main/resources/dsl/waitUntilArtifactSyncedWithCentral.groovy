package dsl

import org.jenkinsci.plugins.fabric8.model.ServiceConstants
import org.jenkinsci.plugins.fabric8.steps.WaitUntilArtifactSyncedWithCentral

def call(WaitUntilArtifactSyncedWithCentral.Arguments config) {
  def flow = new Fabric8Commands()

  // mandatory properties
  def groupId = config.groupId
  def artifactId = config.artifactId
  def version = config.version

  def repo = config.repositoryUrl ?: ServiceConstants.MAVEN_CENTRAL
  def ext = config.extension ?: 'jar'

  if (groupId && artifactId && version) {
    echo "waiting for artifact ${groupId}/${artifactId}/${version}/${ext} to be in repo ${repo}"

    waitUntil {
      retry(3) {
        flow.isArtifactAvailableInRepo(repo, groupId.replaceAll('\\.', '/'), artifactId, version, ext)
      }
    }

    message = "${config.artifactId} ${config.version} released and available in maven central"
    hubotSend message: message, failOnError: false
  } else {
    echo "required properties missing groupId: ${groupId}, artifactId: ${artifactId}, version: ${version}"
  }

}
