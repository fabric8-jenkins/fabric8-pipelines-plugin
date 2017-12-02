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

import io.jenkins.functions.Argument;
import io.jenkins.functions.Step;
import org.jenkinsci.plugins.fabric8.CommandSupport;
import org.jenkinsci.plugins.fabric8.Fabric8Commands;
import org.jenkinsci.plugins.fabric8.helpers.ConfigHelper;
import org.jenkinsci.plugins.fabric8.model.StagedProjectInfo;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Step(displayName = "Stages a release of the maven project into a staging repository")
public class StageProject extends CommandSupport{
    public StageProject() {
    }

    public StageProject(CommandSupport parentStep) {
        super(parentStep);
    }

    public static class Arguments implements Serializable {
        private static final long serialVersionUID = 1L;

        @Argument
        @NotEmpty
        private String project = "";
        @Argument
        private boolean useMavenForNextVersion;
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
        private String stageRepositoryUrl = "https://oss.sonatype.org";
        @Argument
        private String stageServerId = "oss-sonatype-staging";
        @Argument
        private boolean skipTests;
        @Argument
        private boolean disableGitPush = false;
        @Argument
        private List<String> mavenProfiles = new ArrayList<>();


        public static Arguments newInstance(Map<String,Object> map) {
            Arguments answer = new Arguments();
            ConfigHelper.populateBeanFromConfiguration(answer, map);
            return answer;
        }

        public Arguments() {
        }

        public Arguments(String project) {
            this.project = project;
        }

        public boolean isUseMavenForNextVersion() {
            return useMavenForNextVersion;
        }

        public void setUseMavenForNextVersion(boolean useMavenForNextVersion) {
            this.useMavenForNextVersion = useMavenForNextVersion;
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

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
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

        public boolean isSkipTests() {
            return skipTests;
        }

        public void setSkipTests(boolean skipTests) {
            this.skipTests = skipTests;
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

        public boolean isDisableGitPush() {
            return disableGitPush;
        }

        public void setDisableGitPush(boolean disableGitPush) {
            this.disableGitPush = disableGitPush;
        }

        public List<String> getMavenProfiles() {
            return mavenProfiles;
        }

        public void setMavenProfiles(List<String> mavenProfiles) {
            this.mavenProfiles = mavenProfiles;
        }
    }
}
