package dsl

import io.fabric8.utils.Strings
import org.apache.maven.model.Model
import org.jenkinsci.plugins.fabric8.model.ServiceConstants
import org.jenkinsci.plugins.fabric8.model.WaitForArtifactInfo
import org.jenkinsci.plugins.fabric8.steps.ReleaseProject

def call(ReleaseProject.Arguments arguments) {
  echo "releaseProject ${arguments}"

  def flow = new Fabric8Commands()
  def project = arguments.project
  def releaseVersion = arguments.releaseVersion

  String pullRequestId = promoteArtifacts {
    project = project
    releaseVersion = releaseVersion
    repoIds = arguments.repoIds
    useGitTagForNextVersion = arguments.useGitTagForNextVersion
    helmPush = arguments.helmPush
    containerName = arguments.containerName
  }

  def dockerOrganisation = arguments.dockerOrganisation
  def promoteToDockerRegistry = arguments.promoteToDockerRegistry
  def promoteDockerImages = arguments.promoteDockerImages

  if (dockerOrganisation && promoteDockerImages.size() > 0) {
    promoteImages {
      toRegistry = promoteToDockerRegistry
      org = dockerOrganisation
      project = project
      images = promoteDockerImages
      tag = releaseVersion
    }
  }

  def tagDockerImages = arguments.extraImagesToTag

  if (tagDockerImages && tagDockerImages.size() > 0) {
    tagImages {
      images = tagDockerImages
      tag = releaseVersion
    }
  }

  if (pullRequestId != null) {
    waitUntilPullRequestMerged {
      name = project
      prId = pullRequestId
    }
  }

  def waitInfo = arguments.createWaitForArtifactInfo()
  Model mavenProject = flow.loadMavenPom()
  defaultWaitInfoFromPom(waitInfo, mavenProject)

  if (waitInfo.isValid()) {
    waitUntilArtifactSyncedWithCentral {
      repositoryUrl = waitInfo.repositoryUrl
      groupId = waitInfo.groupId
      artifactId = waitInfo.artifactId
      version = releaseVersion
      ext = waitInfo.extension
    }
  }
}

/**
 * If no properties are configured explicitly lets try default them from the pom.xml
 */
def defaultWaitInfoFromPom(WaitForArtifactInfo info, Model mavenProject) {
  if (mavenProject != null) {
    if (Strings.isNullOrBlank(info.groupId)) {
      info.groupId = mavenProject.groupId
    }
    if (Strings.isNullOrBlank(info.artifactId)) {
      info.artifactId = mavenProject.artifactId
    }
    if (Strings.isNullOrBlank(info.extension)) {
      info.extension = "pom";
    }
    if (Strings.isNullOrBlank(info.repositoryUrl)) {
      info.repositoryUrl = ServiceConstants.MAVEN_CENTRAL
    }
  }
}


