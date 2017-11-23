package dsl

import org.jenkinsci.plugins.fabric8.Utils

// The call(body) method in any file in workflowLibs.git/vars is exposed as a
// method with the same name as the file.
def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()


  // TODO
  def utils = createUtils()

  def organisationName = "fabric8io"
  def repoName = '${organisationName}/fabric8-platform'
  def groupId = 'io.fabric8.platform.distro'

  try {
    // TODO
    checkout scm

    if (utils.isCI() || !utils.isCD()) {
      echo 'Performing CI Pipeline'

      sh "mvn clean install -U"

    } else if (utils.isCD()) {
      echo 'Performing CD Pipeline'

      sh "git remote set-url origin git@github.com:${repoName}.git"

      def stagedProject

      stage('Stage') {
        stagedProject = stageProject {
          project = repoName
          useGitTagForNextVersion = true
        }
      }

      stage('Promote') {
        releaseProject {
          stagedProject = stagedProject
          useGitTagForNextVersion = true
          helmPush = false
          groupId = groupId
          githubOrganisation = organisationName
          artifactIdToWatchInCentral = 'distro'
          artifactExtensionToWatchInCentral = 'pom'
          promoteToDockerRegistry = 'docker.io'
          dockerOrganisation = 'fabric8'
          imagesToPromoteToDockerHub = []
          extraImagesToTag = null
        }
      }
/*
      stage('Promote YAMLs') {
        pipeline.promoteYamls(stagedProject[1])
      }
      */
    }
  } catch (err) {
    //hubot room: 'release', message: "${env.JOB_NAME} failed: ${err}"
    error "${err}"
  }
}


def createUtils() {
  def u = new Utils()
  u.setEnv(env.environment)
  return u
}