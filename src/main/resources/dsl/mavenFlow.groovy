package dsl

import io.fabric8.utils.Strings
import org.jenkinsci.plugins.fabric8.FailedBuildException
import org.jenkinsci.plugins.fabric8.ShellFacade
import org.jenkinsci.plugins.fabric8.Utils
import org.jenkinsci.plugins.fabric8.helpers.GitHelper
import org.jenkinsci.plugins.fabric8.helpers.GitRepositoryInfo
import org.jenkinsci.plugins.fabric8.model.StagedProjectInfo
import org.jenkinsci.plugins.fabric8.steps.MavenFlow
import org.jenkinsci.plugins.fabric8.steps.ReleaseProject
import org.jenkinsci.plugins.fabric8.steps.StageProject

def utils

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

    utils = createUtils()
    def branch = findBranch()
    println "setting branch"
    utils.setBranch(branch)

    println "Creating arguments"
    MavenFlow.Arguments arguments = MavenFlow.Arguments.newInstance(config);
    println "have arguments ${arguments}"

    println "current gitCloneUrl = ${arguments.gitCloneUrl}"
    if (!arguments.gitCloneUrl) {
      println "Loading gitCloneUrl"
      arguments.gitCloneUrl = doFindGitCloneURL()
      println "gitCloneUrl now is ${arguments.gitCloneUrl}"
    }

    println "testing CI / CD branch"
    if (isCi(arguments)) {
      ciPipeline(arguments);
    } else if (isCD(arguments)) {
      cdPipeline(arguments);
    } else {
      // for now lets assume a CI pipeline
      ciPipeline(arguments);
    }

    println("Completed")
    if (pauseOnSuccess) {
      input message: 'The build pod has been paused'
    }

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

  def path = sh(script: "pwd", returnStdout: true)
  if (path) {
    println "Currnet path is ${pwd}"
    u.setCurrentPath(path.trim())
  }
  return u
}

boolean isCD(MavenFlow.Arguments arguments) {
  Boolean flag = null
  try {
    flag = utils.isCD();
  } catch (e) {
    error(e)
  }
  if (flag && flag.booleanValue()) {
    return true;
  }
  String organisation = arguments.getCdOrganisation();
  List<String> cdBranches = arguments.getCdBranches();
  println("invoked with organisation " + organisation + " branches " + cdBranches);
  if (cdBranches != null && cdBranches.size() > 0 && Strings.notEmpty(organisation)) {
    def branch = utils.getBranch()
    if (cdBranches.contains(branch)) {
      String gitUrl = arguments.getGitCloneUrl()
      if (Strings.isNotBlank(gitUrl)) {
        GitRepositoryInfo info = GitHelper.parseGitRepositoryInfo(gitUrl);
        if (info != null) {
          boolean answer = organisation.equals(info.getOrganisation());
          if (!answer) {
            println("Not a CD pipeline as the organisation is " + info.getOrganisation() + " instead of " + organisation);
          }
          return answer;
        }
      } else {
        warning("No git URL could be found so assuming not a CD pipeline");
      }
    } else {
      println("branch ${branch} is not in the cdBranches ${cdBranches} so this is a CI pipeline")
    }
  } else {
    warning("No cdOrganisation or cdBranches configured so assuming not a CD pipeline");
  }
  return false;
}

def warning(String message) {
  println "WARNING: ${message}"
}

boolean isCi(MavenFlow.Arguments arguments) {
  boolean value = false
  try {
    value = utils.isCI();
  } catch (e) {
    error(e)
  }
  if (value) {
    return true;
  }

  // TODO for now should we return true if CD is false?
  return !isCD(arguments);
}

def error(Throwable t) {
  println "ERROR: " + t.getMessage()
  t.printStackTrace()
}

def error(String message, Throwable t) {
  println "ERROR: " + message + " " + t.getMessage()
  t.printStackTrace()
}

/**
 * Implements the CI pipeline
 */
Boolean ciPipeline(MavenFlow.Arguments arguments) {
  println("Performing CI pipeline");
  //sh("mvn clean install");
  sh("mvn -version");
  return false;
}

/**
 * Implements the CD pipeline
 */
Boolean cdPipeline(MavenFlow.Arguments arguments) {
  println("Performing CD pipeline");
  String gitCloneUrl = arguments.getGitCloneUrl();
  if (Strings.isNullOrBlank(gitCloneUrl)) {
    error("No gitCloneUrl configured for this pipeline!");
    throw new FailedBuildException("No gitCloneUrl configured for this pipeline!");
  }
  GitRepositoryInfo repositoryInfo = GitHelper.parseGitRepositoryInfo(gitCloneUrl);
  sh("git remote set-url " + gitCloneUrl);

  println "TODO CD Pipeline"
/*
  StageProject.Arguments stageProjectArguments = arguments.createStageProjectArguments(getLogger(), repositoryInfo);
  StagedProjectInfo stagedProject = new StageProject(this).apply(stageProjectArguments);

  ReleaseProject.Arguments releaseProjectArguments = arguments.createReleaseProjectArguments(getLogger(), stagedProject);
  return new ReleaseProject(this).apply(releaseProjectArguments);
*/
}

String doFindGitCloneURL() {
    String text = getGitConfigFile(new File(pwd()));
    if (Strings.isNullOrBlank(text)) {
        text = readFile(".git/config");
    }
    println("\nfindGitCloneURL() text: " + text);
    if (Strings.notEmpty(text)) {
        return GitHelper.extractGitUrl(text);
    }
    return null;
}


String getGitConfigFile(File dir) {
    String path = new File(dir, ".git/config").getAbsolutePath();
    String text = readFile(path);
    if (text != null) {
        text = text.trim();
        if (text.length() > 0) {
            return text;
        }
    }
    File file = dir.getParentFile();
    if (file != null) {
        return getGitConfigFile(file);
    }
    return null;
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