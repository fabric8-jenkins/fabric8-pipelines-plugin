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
import org.kohsuke.github.GHPullRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

@Step(displayName = "Promotes maven archetypes to a nexus repository")
public class PromoteArtifacts extends CommandSupport implements Function<PromoteArtifacts.Arguments, GHPullRequest> {

    public PromoteArtifacts() {
    }

    public PromoteArtifacts(CommandSupport parentStep) {
        super(parentStep);
    }

    @Override
    @Step
    public GHPullRequest apply(Arguments config) {
        final String project = config.getProject();
        final String version = config.getVersion();
        final List<String> repoIds = config.getRepoIds();
        final String containerName = config.containerName;

        container(containerName, (Callable<GHPullRequest>) () -> {
            sh("chmod 600 /root/.ssh-git/ssh-key");
            sh("chmod 600 /root/.ssh-git/ssh-key.pub");
            sh("chmod 700 /root/.ssh-git");

            Fabric8Commands flow = new Fabric8Commands(PromoteArtifacts.this);

            echo("About to release " + project + " repo ids " + repoIds);
            for (String repoId : repoIds) {
                flow.releaseSonartypeRepo(repoId);
            }

            if (config.isHelmPush()) {
                flow.helm();
            }

            if (config.isUpdateNextDevelopmentVersion()) {
                String args = config.getUpdateNextDevelopmentVersionArguments();
                if (args == null) {
                    args = "";
                }
                flow.updateNextDevelopmentVersion(version, args);
                return flow.createPullRequest("[CD] Release " + version, project, "release-v" + version);
            }
            return null;
        });
        return null;
    }

    public static class Arguments implements Serializable {
        private static final long serialVersionUID = 1L;

        @Argument
        private String project = "";
        @Argument
        private String version = "";
        @Argument
        private List<String> repoIds = new ArrayList<>();
        @Argument
        private String containerName = "maven";
        @Argument
        private boolean helmPush;
        @Argument
        private boolean updateNextDevelopmentVersion;
        @Argument
        private String updateNextDevelopmentVersionArguments = "";

        public Arguments() {
        }

        public Arguments(String project, String version) {
            this.project = project;
            this.version = version;
        }

        public Arguments(String project, String version, List<String> repoIds) {
            this.project = project;
            this.version = version;
            this.repoIds = repoIds;
        }

        public Arguments(String project, String version, List<String> repoIds, String containerName, boolean helmPush, boolean updateNextDevelopmentVersion, String updateNextDevelopmentVersionArguments) {
            this.project = project;
            this.version = version;
            this.repoIds = repoIds;
            this.containerName = containerName;
            this.helmPush = helmPush;
            this.updateNextDevelopmentVersion = updateNextDevelopmentVersion;
            this.updateNextDevelopmentVersionArguments = updateNextDevelopmentVersionArguments;
        }

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<String> getRepoIds() {
            return repoIds;
        }

        public void setRepoIds(List<String> repoIds) {
            this.repoIds = repoIds;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public boolean isHelmPush() {
            return helmPush;
        }

        public void setHelmPush(boolean helmPush) {
            this.helmPush = helmPush;
        }

        public boolean isUpdateNextDevelopmentVersion() {
            return updateNextDevelopmentVersion;
        }

        public void setUpdateNextDevelopmentVersion(boolean updateNextDevelopmentVersion) {
            this.updateNextDevelopmentVersion = updateNextDevelopmentVersion;
        }

        public String getUpdateNextDevelopmentVersionArguments() {
            return updateNextDevelopmentVersionArguments;
        }

        public void setUpdateNextDevelopmentVersionArguments(String updateNextDevelopmentVersionArguments) {
            this.updateNextDevelopmentVersionArguments = updateNextDevelopmentVersionArguments;
        }
    }
}
