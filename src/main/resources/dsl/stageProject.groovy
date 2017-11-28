package dsl

def call(Map config = [:]) {
  echo "stageProject ${config}"

/*
  StageProject.Arguments arguments = StageProject.Arguments.newInstance(config);
  println "have arguments ${arguments}"
*/

  //return doStage(arguments)

  //def flow = new Fabric8Commands()
  println "stageProject about to create Fabric8Commands"
  def flow
  try {
    flow = new Fabric8Commands()
    //flow = fabric8Commands()
  } catch (e) {
    logError(e)
  }
  println "stageProject created flow ${flow}"

  def repoId
  def releaseVersion
  def extraStageImages = config.extraImagesToStage ?: []
  def extraSetVersionArgs = config.setVersionExtraArgs ?: ""
  def containerName = config.containerName ?: 'maven'
  def clientsContainerName = config.clientsContainerName ?: 'clients'

  container(name: clientsContainerName) {

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

    // TODO
    //sh "git remote set-url origin git@github.com:${config.project}.git"

    def currentVersion = flow.getProjectVersion()

    println "currentVersion ${currentVersion}"
    
    flow.setupWorkspaceForRelease(config.project, config.useGitTagForNextVersion, extraSetVersionArgs, currentVersion, containerName)

    repoId = flow.stageSonartypeRepo()
    releaseVersion = flow.getProjectVersion()

    // lets avoide the stash / unstash for now as we're not using helm ATM
    //stash excludes: '*/src/', includes: '**', name: "staged-${config.project}-${releaseVersion}".hashCode().toString()

    if (!config.useGitTagForNextVersion){
      flow.updateGithub ()
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

  return [config.project, releaseVersion, repoId]
}




//StagedProjectInfo doStage(StageProject.Arguments config) {
//    final Fabric8Commands flow = new Fabric8Commands();
//
//    final AtomicReference<List<String>> repoIdsRef = new AtomicReference<>();
//    final AtomicReference<String> releaseVersionRef = new AtomicReference<>();
//
//    final List<String> extraImagesToStage = config.getExtraImagesToStage();
//    final String containerName = config.getContainerName();
//    final String project = config.getProject();
//
//    container(containerName) {
//        sh("chmod 600 /root/.ssh-git/ssh-key");
//        sh("chmod 600 /root/.ssh-git/ssh-key.pub");
//        sh("chmod 700 /root/.ssh-git");
//        sh("chmod 600 /home/jenkins/.gnupg/pubring.gpg");
//        sh("chmod 600 /home/jenkins/.gnupg/secring.gpg");
//        sh("chmod 600 /home/jenkins/.gnupg/trustdb.gpg");
//        sh("chmod 700 /home/jenkins/.gnupg");
//
//        String currentVersion = flow.getProjectVersion();
//
//        boolean useGitTagForNextVersion = config.isUseGitTagForNextVersion();
//        flow.setupWorkspaceForRelease(project, useGitTagForNextVersion, config.getExtraSetVersionArgs(), currentVersion);
//
//        repoIdsRef.set(flow.stageSonartypeRepo());
//        releaseVersionRef.set(flow.getProjectVersion());
//
//        // lets avoide the stash / unstash for now as we're not using helm ATM
//        //stash excludes: '*/src/', includes: '**', name: "staged-${config.project}-${releaseVersion}".hashCode().toString()
//
//        if (!useGitTagForNextVersion) {
//            return flow.updateGithub();
//        }
//        return null;
//    });
//
//    String releaseVersion = releaseVersionRef.get();
//    if (extraImagesToStage != null) {
//        new StageExtraImages(this).apply(releaseVersion, extraImagesToStage);
//    }
//    return new StagedProjectInfo(project, releaseVersion, repoIdsRef.get());
//}

// TODO common stuff
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