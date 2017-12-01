package dsl

def call(Map config = [:]) {
  echo "releaseProject ${config}"

  def projectName = config.project
  def releaseVersion = config.releaseVersion
  def repoIds = config.repoIds
  def promoteDockerImages = config.imagesToPromoteToDockerHub ?: []
  def tagDockerImages = config.extraImagesToTag ?: []
  def container = config.containerName ?: 'maven'
  def dockerOrganisation = config.dockerOrganisation

  String pullRequestId = promoteArtifacts {
    project = projectName
    releaseVersion = releaseVersion
    repoIds = repoIds
    useGitTagForNextVersion = config.useGitTagForNextVersion
    helmPush = config.helmPush
    containerName = container
  }

  if (dockerOrganisation && promoteDockerImages.size() > 0) {
    promoteImages {
      toRegistry = config.promoteToDockerRegistry
      org = dockerOrganisation
      project = projectName
      images = promoteDockerImages
      tag = releaseVersion
    }
  }

  if (tagDockerImages && tagDockerImages.size() > 0) {
    tagImages {
      images = tagDockerImages
      tag = releaseVersion
    }
  }

  if (pullRequestId != null) {
    waitUntilPullRequestMerged {
      name = projectName
      prId = pullRequestId
    }
  }

  // TODO default these from the pom if not specified...
  def centralRepo = 'http://central.maven.org/maven2/'
  def groupId = config.groupId
  def artifactIdToWatchInCentral = config.artifactIdToWatchInCentral
  def artifactExtensionToWatchInCentral = config.artifactExtensionToWatchInCentral

  if (groupId && artifactIdToWatchInCentral && artifactExtensionToWatchInCentral) {
    waitUntilArtifactSyncedWithCentral {
      repo = centralRepo
      groupId = groupId
      artifactId = artifactIdToWatchInCentral
      version = releaseVersion
      ext = artifactExtensionToWatchInCentral
    }
  }
}
