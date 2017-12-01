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

import com.google.common.base.Strings;
import io.jenkins.functions.Argument;
import io.jenkins.functions.Step;
import org.jenkinsci.plugins.fabric8.CommandSupport;
import org.jenkinsci.plugins.fabric8.Fabric8Commands;
import org.jenkinsci.plugins.fabric8.model.ServiceConstants;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.function.Function;

/**
 * Waits for a maven artifact to be in the maven central repository
 */
@Step(displayName = "Waits for an artifact to be synchronized to a central registry")
public class WaitUntilArtifactSyncedWithCentral extends CommandSupport implements Function<WaitUntilArtifactSyncedWithCentral.Arguments, String> {
    public WaitUntilArtifactSyncedWithCentral() {
    }

    public WaitUntilArtifactSyncedWithCentral(CommandSupport parentStep) {
        super(parentStep);
    }

    @Override
    @Step
    public String apply(Arguments config) {
        final Fabric8Commands flow = new Fabric8Commands(this);

        final String groupId = config.groupId;
        final String artifactId = config.artifactId;
        final String version = config.version;
        final String ext = config.extension;

        if (Strings.isNullOrEmpty(groupId) || Strings.isNullOrEmpty(artifactId) || Strings.isNullOrEmpty(version)) {
            error("Must specify full maven coordinates but was given: " + config);
            return null;
        }

        waitUntil(() -> retry(3, () -> flow.isArtifactAvailableInRepo(config.repositoryUrl, groupId, artifactId, version, ext)));

        String message = "" + groupId + "/" + artifactId + " " + version + " released and available in maven central";
        echo(message);
        hubotSend(message);
        return null;
    }

    public static class Arguments implements Serializable {
        private static final long serialVersionUID = 1L;

        @Argument
        private String repositoryUrl = ServiceConstants.MAVEN_CENTRAL;
        @Argument
        @NotEmpty
        private String groupId = "";
        @Argument
        @NotEmpty
        private String artifactId = "";
        @Argument
        @NotEmpty
        private String version = "";
        @Argument
        private String extension = "jar";

        public Arguments() {
        }

        public Arguments(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        @Override
        public String toString() {
            return "Arguments{" +
                    "repo='" + repositoryUrl + '\'' +
                    ", groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", version='" + version + '\'' +
                    ", ext='" + extension + '\'' +
                    '}';
        }


        /**
         * Returns true if the properties are populated with enough values to watch for an artifact
         */
        public boolean isValid() {
            return io.fabric8.utils.Strings.notEmpty(repositoryUrl) && io.fabric8.utils.Strings.notEmpty(groupId) && io.fabric8.utils.Strings.notEmpty(artifactId) && io.fabric8.utils.Strings.notEmpty(extension);
        }


        public String getRepositoryUrl() {
            return repositoryUrl;
        }

        public void setRepositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }
    }

}
