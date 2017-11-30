package dsl


def call(Map config = [:]) {
  echo "stageProject ${config}"

/*
  StageProject.Arguments arguments = StageProject.Arguments.newInstance(config);
  println "have arguments ${arguments}"
*/

  println "stageProject about to create Fabric8Commands"
  def flow
  try {
    flow = new Fabric8Commands()
  } catch (e) {
    logError(e)
  }
  println "stageProject created flow ${flow}"

  def extraStageImages = config.extraImagesToStage ?: []
  def extraSetVersionArgs = config.setVersionExtraArgs ?: ""
  def containerName = config.containerName ?: 'maven'
  def clientsContainerName = config.clientsContainerName ?: 'clients'
  def useMavenForNextVersion = config.useMavenForNextVersion ?: false
  def repositoryUrl = config.repositoryUrl ?: null
  def useStaging = config.useStaging ?: false

  // stage to repo
  println "about to try call setupStageWorkspace"

  setupStageWorkspace(flow, useMavenForNextVersion, extraSetVersionArgs, containerName, clientsContainerName)

  echo "called setupStageWorkspace"

  // TODO determine the stage repo / id / whether to use staging... - maybe default to staging if sonatype or configured?
  def nexusUrl="https://oss.sonatype.org"
  def serverId = "oss-sonatype-staging"
  def skipTests = config.skipTests ?: false

  def repoIds = []
  if (useStaging) {
    echo "using staging to the repository: ${serverId} at ${nexusUrl}"
    repoIds = stageSonartypeRepo(flow, serverId, nexusUrl)
  } else {
    echo "deploying to local artifact-repository"
    mavenDeploy(skipTests)
  }

  def releaseVersion = flow.getProjectVersion()

  container(clientsContainerName) {
    if (fileExists("root/.ssh-git")) {
      sh 'chmod 600 /root/.ssh-git/ssh-key'
      sh 'chmod 600 /root/.ssh-git/ssh-key.pub'
      sh 'chmod 700 /root/.ssh-git'
    }

    if (fileExists("/home/jenkins/.gnupg")) {
      sh 'chmod 600 /home/jenkins/.gnupg/pubring.gpg'
      sh 'chmod 600 /home/jenkins/.gnupg/secring.gpg'
      sh 'chmod 600 /home/jenkins/.gnupg/trustdb.gpg'
      sh 'chmod 700 /home/jenkins/.gnupg'
    }
  }
  // TODO
  //sh "git remote set-url origin git@github.com:${config.project}.git"

  // lets avoide the stash / unstash for now as we're not using helm ATM
  //stash excludes: '*/src/', includes: '**', name: "staged-${config.project}-${releaseVersion}".hashCode().toString()

  if (!config.useGitTagForNextVersion) {
    container(clientsContainerName) {
      flow.updateGithub()
    }
  }

/*
  if (config.extraImagesToStage != null){
    stageExtraImages {
      images = extraStageImages
      tag = releaseVersion
    }
  }
*/

  return [config.project, releaseVersion, repoIds]
}


def stageSonartypeRepo(Fabric8Commands flow, String serverId, String nexusUrl) {
    try {
        def registryHost = env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST
        if (!registryHost) {
            echo "WARNING you don't seem to be running the fabric8-docker-registry service!!!"
            registryHost = "http://fabric8-docker-registry"
        }
        def registryPort = env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT
        if (registryPort) {
            registryHost = "${registryHost}:${registryPort}"
        }

        echo "using docker registry: ${registryHost}"

        sh "mvn clean -B"
        sh "mvn -V -B -e -U install org.sonatype.plugins:nexus-staging-maven-plugin:1.6.7:deploy -P release -P openshift -DnexusUrl=https://oss.sonatype.org -DserverId=oss-sonatype-staging -Ddocker.push.registry=${registryHost}"

        // lets not archive artifacts until we if we just use nexus or a content repo
        //step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])

    } catch (err) {
        hubotSend room: 'release', message: "Release failed when building and deploying to Nexus ${err}", failOnError: false
        currentBuild.result = 'FAILURE'
        error "ERROR Release failed when building and deploying to Nexus ${err}"
    }
    // the sonartype staging repo id gets written to a file in the workspace
    return flow.getRepoIds()
}

def mavenDeploy(skipTests) {
  sh "mvn clean -B -e -U deploy -Dmaven.test.skip=${skipTests} -P openshift,artifact-repository"
}
def setupStageWorkspace(Fabric8Commands flow, boolean useMavenForNextVersion, String mvnExtraArgs, String containerName, String clientsContainerName) {
  container(clientsContainerName) {
    sh "git config user.email fabric8-admin@googlegroups.com"
    sh "git config user.name fabric8-release"

    sh 'chmod 600 /root/.ssh-git/ssh-key'
    sh 'chmod 600 /root/.ssh-git/ssh-key.pub'
    sh 'chmod 700 /root/.ssh-git'

    if (fileExists("/home/jenkins/.gnupg/pubring.gpg")) {
      sh 'chmod 600 /home/jenkins/.gnupg/pubring.gpg'
      sh 'chmod 600 /home/jenkins/.gnupg/secring.gpg'
      sh 'chmod 600 /home/jenkins/.gnupg/trustdb.gpg'
      sh 'chmod 700 /home/jenkins/.gnupg'
    }

    sh "git tag -d \$(git tag)"
    sh "git fetch --tags"

    if (useMavenForNextVersion) {
      container(containerName) {
        sh 'mvn -B build-helper:parse-version versions:set -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} ' + mvnExtraArgs
      }
    } else {
      def newVersion = newVersionUsingSemVer(flow, clientsContainerName)
      echo "New release version ${newVersion}"
      container(containerName) {
        sh "mvn -B -U versions:set -DnewVersion=${newVersion} " + mvnExtraArgs
      }

      sh "git commit -a -m 'release ${newVersion}'"
      flow.pushTag(newVersion)
    }

    def releaseVersion = flow.getProjectVersion()

    // delete any previous branches of this release
    try {
      sh "git checkout -b release-v${releaseVersion}"
    } catch (err) {
      sh "git branch -D release-v${releaseVersion}"
      sh "git checkout -b release-v${releaseVersion}"
    }
  }
  return false
}

String newVersionUsingSemVer(Fabric8Commands flow, String clientsContainerName) {
  container(clientsContainerName) {
    return shOutput("semver-release-version --folder " + pwd()).trim();
  }
}


def logError(Throwable t) {
  println "ERROR: " + t.getMessage()
  t.printStackTrace()
}

def logError(String message, Throwable t) {
  println "ERROR: " + message + " " + t.getMessage()
  t.printStackTrace()
}

def warning(String message) {
  println "WARNING: ${message}"
}

/**
 * Returns the trimmed text output of the given command
 */
String shOutput(String command) {
  String answer = sh(script: command, returnStdout: true)
  if (answer != null) {
    return answer.trim();
  }
  return null
}
