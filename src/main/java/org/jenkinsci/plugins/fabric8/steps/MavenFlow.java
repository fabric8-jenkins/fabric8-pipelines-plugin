/**
 * Copyright (C) Original Authors 2017
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.fabric8.steps;

import com.cloudbees.groovy.cps.NonCPS;
import io.fabric8.utils.Strings;
import io.jenkins.functions.Argument;
import io.jenkins.functions.Step;
import org.jenkinsci.plugins.fabric8.CommandSupport;
import org.jenkinsci.plugins.fabric8.Utils;
import org.jenkinsci.plugins.fabric8.helpers.ConfigHelper;
import org.jenkinsci.plugins.fabric8.helpers.GitHelper;
import org.jenkinsci.plugins.fabric8.helpers.GitRepositoryInfo;
import org.jenkinsci.plugins.fabric8.model.ServiceConstants;
import org.jenkinsci.plugins.fabric8.model.StagedProjectInfo;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Performs a full CI / CD pipeline for maven projects
 */
@Step(displayName = "Full CI / CD pipeline for maven projects")
public class MavenFlow extends CommandSupport implements Function<MavenFlow.Arguments, Boolean> {
    private final Utils utils;


    public MavenFlow(Utils utils) {
        super(utils);
        this.utils = utils;
    }

    @NonCPS
    public static Boolean perform(Utils utils, Map map) {
        Arguments arguments = Arguments.newInstance(map);
        MavenFlow flow = new MavenFlow(utils);
        return flow.apply(arguments);
    }

    public Boolean apply() {
        return apply(createArguments());
    }

    protected Arguments createArguments() {
        Arguments arguments = new Arguments();
        // TODO
        return arguments;
    }

    @Override
    @Step
    public Boolean apply(Arguments arguments) {
        // TODO
        //checkoutScm();
        if (isCi(arguments)) {
            return ciPipeline(arguments);
        } else if (isCD(arguments)) {
            return cdPipeline(arguments);
        } else {
            // for now lets assume a CI pipeline
            return ciPipeline(arguments);
        }
    }

    public boolean isCD(Arguments arguments) {
        Boolean flag = utils.isCD();
        if (flag && flag.booleanValue()) {
            return true;
        }
        String organisation = arguments.getCdOrganisation();
        List<String> cdBranches = arguments.getCdBranches();
        System.out.println("=== invoked with organisation " + organisation + " branches " + cdBranches);
        if (cdBranches != null && cdBranches.size() > 0 && Strings.notEmpty(organisation)) {
            if (cdBranches.contains(utils.getBranch())) {
                String gitUrl = findGitCloneURL(arguments);
                if (Strings.isNotBlank(gitUrl)) {
                    GitRepositoryInfo info = GitHelper.parseGitRepositoryInfo(gitUrl);
                    if (info != null) {
                        boolean answer = organisation.equals(info.getOrganisation());
                        if (!answer) {
                            echo("Not a CD pipeline as the organisation is " + info.getOrganisation() + " instead of " + organisation);
                        }
                        return answer;
                    }
                } else {
                    warning("No git URL could be found so assuming not a CD pipeline");
                }
            }
        } else {
            warning("No cdOrganisation or cdBranches configured so assuming not a CD pipeline");
        }
        return false;
    }

    public boolean isCi(Arguments arguments) {
        boolean value = utils.isCI();
        if (value) {
            return true;
        }

        // TODO for now should we return true if CD is false?
        return !isCD(arguments);
    }

    public String findGitCloneURL(Arguments arguments) {
        String answer = arguments.getGitCloneUrl();
        if (Strings.isNullOrBlank(answer)) {
            answer = utils.findGitCloneURL();
            arguments.setGitCloneUrl(answer);
        }
        return answer;
    }

    /**
     * Implements the CI pipeline
     */
    public Boolean ciPipeline(Arguments arguments) {
        echo("Performing CI pipeline");
        //sh("mvn clean install");
        sh("mvn -version");
        return false;
    }

    /**
     * Implements the CD pipeline
     */
    public Boolean cdPipeline(Arguments arguments) {
/*
        echo("Performing CD pipeline");
        String gitCloneUrl = arguments.getGitCloneUrl();
        if (Strings.isNullOrBlank(gitCloneUrl)) {
            error("No gitCloneUrl configured for this pipeline!");
            throw new FailedBuildException("No gitCloneUrl configured for this pipeline!");
        }
        GitRepositoryInfo repositoryInfo = GitHelper.parseGitRepositoryInfo(gitCloneUrl);
        sh("git remote set-url " + gitCloneUrl);
        StageProject.Arguments stageProjectArguments = arguments.createStageProjectArguments(repositoryInfo);
        StagedProjectInfo stagedProject = new StageProject(this).apply(stageProjectArguments);

        ReleaseProject.Arguments releaseProjectArguments = arguments.createReleaseProjectArguments(stagedProject);
        return new ReleaseProject(this).apply(releaseProjectArguments);
*/
        return false;
    }

    public static class Arguments implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotEmpty
        @Argument
        private String gitCloneUrl = "";
        @NotEmpty
        @Argument
        private String cdOrganisation = "";
        @Argument
        private List<String> cdBranches = new ArrayList<>();

        @Argument
        private boolean pauseOnFailure = false;
        @Argument
        private boolean pauseOnSuccess = false;

        @Argument
        private boolean useMavenForNextVersion = false;
        @Argument
        private String extraSetVersionArgs = "";
        @Argument
        private List<String> extraImagesToStage = new ArrayList<>();
        @Argument
        private String containerName = "maven";
        @Argument
        private String clientsContainerName = "clients";
        @Argument
        private boolean useStaging;
        @Argument
        private String stageRepositoryUrl = "";
        @Argument
        private String stageServerId = "";
        @Argument
        private boolean skipTests;
        @Argument
        private boolean useSonatype;

        @Argument
        private boolean updateNextDevelopmentVersion;

        @Argument
        private String dockerOrganisation = "";
        @Argument
        private String promoteToDockerRegistry = "";
        @Argument
        private List<String> promoteDockerImages = new ArrayList<>();
        @Argument
        private List<String> extraImagesToTag = new ArrayList<>();
        @Argument
        private String repositoryToWaitFor;
        @Argument
        private String groupId = "";
        @Argument
        private String artifactExtensionToWaitFor = "";
        @Argument
        private String artifactIdToWaitFor = "";

        
        public static Arguments newInstance(Map map) {
            Arguments arguments = new Arguments();
            ConfigHelper.populateBeanFromConfiguration(arguments, map);
            return arguments;
        }

        public String getGitCloneUrl() {
            return gitCloneUrl;
        }

        public void setGitCloneUrl(String gitCloneUrl) {
            this.gitCloneUrl = gitCloneUrl;
        }

        public StageProject.Arguments createStageProjectArguments(GitRepositoryInfo repositoryInfo) {
            StageProject.Arguments answer = new StageProject.Arguments(repositoryInfo.getProject());
            if (useSonatype) {
                if (Strings.isNullOrBlank(stageRepositoryUrl)) {
                    stageRepositoryUrl = ServiceConstants.SONATYPE_REPOSITORY_URL;
                }
                if (Strings.isNullOrBlank(stageServerId)) {
                    stageServerId = useStaging ? ServiceConstants.SONATYPE_STAGING_SERVER_ID : ServiceConstants.SONATYPE_SERVER_ID;
                }
            }
            if (Strings.isNullOrBlank(repositoryToWaitFor)) {
                if (useSonatype) {
                    repositoryToWaitFor = ServiceConstants.MAVEN_CENTRAL;
                } else {
                    repositoryToWaitFor = ServiceConstants.ARTIFACT_REPOSITORY_RELEASE_URL;
                }
            }

            answer.setUseMavenForNextVersion(useMavenForNextVersion);
            answer.setExtraSetVersionArgs(extraSetVersionArgs);
            answer.setExtraImagesToStage(extraImagesToStage);
            answer.setUseStaging(useStaging);
            answer.setStageRepositoryUrl(stageRepositoryUrl);
            answer.setStageServerId(stageServerId);
            answer.setSkipTests(skipTests);
            if (Strings.notEmpty(containerName)) {
                answer.setContainerName(containerName);
            }
            if (Strings.notEmpty(clientsContainerName)) {
                answer.setClientsContainerName(clientsContainerName);
            }
            return answer;
        }

        public ReleaseProject.Arguments createReleaseProjectArguments(StagedProjectInfo stagedProject) {
            ReleaseProject.Arguments answer = new ReleaseProject.Arguments(stagedProject);
            if (Strings.notEmpty(containerName)) {
                answer.setContainerName(containerName);
            }
            answer.setReleaseVersion(stagedProject.getReleaseVersion());
            answer.setProject(stagedProject.getProject());
            answer.setRepoIds(stagedProject.getRepoIds());

            answer.setArtifactExtensionToWaitFor(getArtifactExtensionToWaitFor());
            answer.setArtifactIdToWaitFor(getArtifactIdToWaitFor());
            answer.setDockerOrganisation(getDockerOrganisation());
            answer.setExtraImagesToTag(getExtraImagesToTag());
            answer.setGroupId(getGroupId());
            answer.setPromoteDockerImages(getPromoteDockerImages());
            answer.setPromoteToDockerRegistry(getPromoteToDockerRegistry());
            answer.setRepositoryToWaitFor(getRepositoryToWaitFor());
            answer.setUpdateNextDevelopmentVersion(isUpdateNextDevelopmentVersion());
            return answer;
        }

        public String getExtraSetVersionArgs() {
            return extraSetVersionArgs;
        }

        public void setExtraSetVersionArgs(String extraSetVersionArgs) {
            this.extraSetVersionArgs = extraSetVersionArgs;
        }

        public List<String> getExtraImagesToStage() {
            return extraImagesToStage;
        }

        public void setExtraImagesToStage(List<String> extraImagesToStage) {
            this.extraImagesToStage = extraImagesToStage;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public String getDockerOrganisation() {
            return dockerOrganisation;
        }

        public void setDockerOrganisation(String dockerOrganisation) {
            this.dockerOrganisation = dockerOrganisation;
        }

        public String getPromoteToDockerRegistry() {
            return promoteToDockerRegistry;
        }

        public void setPromoteToDockerRegistry(String promoteToDockerRegistry) {
            this.promoteToDockerRegistry = promoteToDockerRegistry;
        }

        public List<String> getPromoteDockerImages() {
            return promoteDockerImages;
        }

        public void setPromoteDockerImages(List<String> promoteDockerImages) {
            this.promoteDockerImages = promoteDockerImages;
        }

        public List<String> getExtraImagesToTag() {
            return extraImagesToTag;
        }

        public void setExtraImagesToTag(List<String> extraImagesToTag) {
            this.extraImagesToTag = extraImagesToTag;
        }

        public String getRepositoryToWaitFor() {
            return repositoryToWaitFor;
        }

        public void setRepositoryToWaitFor(String repositoryToWaitFor) {
            this.repositoryToWaitFor = repositoryToWaitFor;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactExtensionToWaitFor() {
            return artifactExtensionToWaitFor;
        }

        public void setArtifactExtensionToWaitFor(String artifactExtensionToWaitFor) {
            this.artifactExtensionToWaitFor = artifactExtensionToWaitFor;
        }

        public String getArtifactIdToWaitFor() {
            return artifactIdToWaitFor;
        }

        public void setArtifactIdToWaitFor(String artifactIdToWaitFor) {
            this.artifactIdToWaitFor = artifactIdToWaitFor;
        }

        public boolean isPauseOnFailure() {
            return pauseOnFailure;
        }

        public void setPauseOnFailure(boolean pauseOnFailure) {
            this.pauseOnFailure = pauseOnFailure;
        }

        public boolean isPauseOnSuccess() {
            return pauseOnSuccess;
        }

        public void setPauseOnSuccess(boolean pauseOnSuccess) {
            this.pauseOnSuccess = pauseOnSuccess;
        }

        public String getCdOrganisation() {
            return cdOrganisation;
        }

        public void setCdOrganisation(String cdOrganisation) {
            this.cdOrganisation = cdOrganisation;
        }

        public List<String> getCdBranches() {
            return cdBranches;
        }

        public void setCdBranches(List<String> cdBranches) {
            this.cdBranches = cdBranches;
        }

        public boolean isUseMavenForNextVersion() {
            return useMavenForNextVersion;
        }

        public void setUseMavenForNextVersion(boolean useMavenForNextVersion) {
            this.useMavenForNextVersion = useMavenForNextVersion;
        }

        public String getClientsContainerName() {
            return clientsContainerName;
        }

        public void setClientsContainerName(String clientsContainerName) {
            this.clientsContainerName = clientsContainerName;
        }

        public boolean isUseStaging() {
            return useStaging;
        }

        public void setUseStaging(boolean useStaging) {
            this.useStaging = useStaging;
        }

        public String getStageRepositoryUrl() {
            return stageRepositoryUrl;
        }

        public void setStageRepositoryUrl(String stageRepositoryUrl) {
            this.stageRepositoryUrl = stageRepositoryUrl;
        }

        public String getStageServerId() {
            return stageServerId;
        }

        public void setStageServerId(String stageServerId) {
            this.stageServerId = stageServerId;
        }

        public boolean isSkipTests() {
            return skipTests;
        }

        public void setSkipTests(boolean skipTests) {
            this.skipTests = skipTests;
        }

        public boolean isUseSonatype() {
            return useSonatype;
        }

        public void setUseSonatype(boolean useSonatype) {
            this.useSonatype = useSonatype;
        }

        public boolean isUpdateNextDevelopmentVersion() {
            return updateNextDevelopmentVersion;
        }

        public void setUpdateNextDevelopmentVersion(boolean updateNextDevelopmentVersion) {
            this.updateNextDevelopmentVersion = updateNextDevelopmentVersion;
        }
    }
}
