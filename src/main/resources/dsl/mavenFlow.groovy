package dsl

import com.cloudbees.groovy.cps.NonCPS
import org.jenkinsci.plugins.fabric8.FileReadFacade
import org.jenkinsci.plugins.fabric8.ShellFacade
import org.jenkinsci.plugins.fabric8.Utils
import org.jenkinsci.plugins.fabric8.steps.MavenFlow

// The call(body) method in any file in workflowLibs.git/vars is exposed as a
// method with the same name as the file.
def call(Map config = [:]) {
/*
def call(Map config = [:], body) {
*/
/*
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
*/
/*
  body()
*/

  echo "mavenFlow ${config}"


  def pauseOnFailure = config.get('pauseOnFailure', false)
  def pauseOnSuccess = config.get('pauseOnSuccess', false)

/*
  def arguments = new MavenFlow.Arguments(config)
  def pauseOnFailure = arguments.isPauseOnFailure()
  def pauseOnSuccess = arguments.isPauseOnSuccess()
*/
/*
/*
  def organisationName = "fabric8io"
  def repoName = '${organisationName}/fabric8-platform'
  def groupId = 'io.fabric8.platform.distro'
*/

  try {
    checkout scm

    def utils = createUtils()
    def branch = findBranch()
    utils.setBranch(branch)

    MavenFlow.perform(utils, config);

    if (pauseOnSuccess) {
      input message: 'The build pod has been paused'
    }

/*    if (utils.isCI() || !utils.isCD()) {
      echo 'Performing CI Pipeline'

      sh "mvn clean install -U"

    } else if (utils.isCD()) {
      echo 'Performing CD Pipeline'

      // TODO can we figure this out from the current .git folder??
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
      }*/
/*
      stage('Promote YAMLs') {
        pipeline.promoteYamls(stagedProject[1])
      }
      */
  } catch (err) {
    //hubot room: 'release', message: "${env.JOB_NAME} failed: ${err}"
    error "${err}"

    if (pauseOnFailure) {
      input message: 'The build pod has been paused'
    }
  }
}


Utils createUtils() {
  def u = new Utils()
  u.updateEnvironment(env)

  u.setShellFacade({ String cmd, boolean returnOutput, String containerName ->
    if (containerName) {
      def answer
      container(containerName) {
        answer = sh(script: cmd, returnStdout: returnOutput).toString().trim()
      }
      return answer
    } else {
      return sh(script: cmd, returnStdout: returnOutput).toString().trim()
    }
  } as ShellFacade)

  u.setFileReadFacade({ String path ->
    return doReadFile(path)
  } as FileReadFacade)

  def path = sh(script: "pwd", returnStdout: true)
  if (path) {
    println "Currnet path is ${pwd}"
    u.setCurrentPath(path.trim())
  }
  return u
}

String doReadFile(String path) {
  println("mavenFlow.doReadFile on ${path}")
  def answer = readFile(path)
  println("mavenFlow.doReadFile result: ${answer}")
  if (answer != null) {
    return answer.toString()
  }
  return null
}

String findBranch() {
  def branch = env.BRANCH_NAME
  if (!branch) {
    container("clients") {
      try {
        echo("output of git --version: " + sh(script: "git --version", returnStdout: true));
        echo("pwd: " + sh(script: "pwd", returnStdout: true));
      } catch (e) {
        error("Failed to invoke git --version: " + e, e);
      }
      if (!branch) {
        def head = null
        try {
          head = sh(script: "git rev-parse HEAD", returnStdout: true)
        } catch (e) {
          error("Failed to load: git rev-parse HEAD: " + e, e)
        }
        if (head) {
          head = head.trim()
          try {
            def text = sh(script: "git ls-remote --heads origin | grep ${head} | cut -d / -f 3", returnStdout: true)
            if (text) {
              branch = text.trim();
            }
          } catch (e) {
            error("\nUnable to get git branch: " + e, e);
          }
        }
      }
      if (!branch) {
        try {
          def text = sh(script: "git symbolic-ref --short HEAD", returnStdout: true)
          if (text) {
            branch = text.trim();
          }
        } catch (e) {
          error("\nUnable to get git branch and in a detached HEAD. You may need to select Pipeline additional behaviour and \'Check out to specific local branch\': " + e, e);
        }
      }
    }
    echo "Found branch ${branch}"
    return branch;
  }
}