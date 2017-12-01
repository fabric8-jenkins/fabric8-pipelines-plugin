/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.fabric8.model;

import io.fabric8.utils.Strings;

import java.io.Serializable;


/**
 * Parameters to wait for artifacts in maven repositories
 */
public class WaitForArtifactInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String repositoryUrl;
    private String groupId;
    private String artifactId;
    private String extension;

    public WaitForArtifactInfo(String repositoryUrl, String groupId, String artifactId, String extension) {
        if (Strings.isNullOrBlank(repositoryUrl)) {
            repositoryUrl = ServiceConstants.MAVEN_CENTRAL;
        }
        if (Strings.isNullOrBlank(extension)) {
            extension = "jar";
        }
        this.repositoryUrl = repositoryUrl;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.extension = extension;
    }

    @Override
    public String toString() {
        return "WaitForArtifactInfo{" +
                "repositoryUrl='" + repositoryUrl + '\'' +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", extension='" + extension + '\'' +
                '}';
    }

    /**
     * Returns true if the properties are populated with enough values to watch for an artifact
     */
    public boolean isValid() {
        return Strings.notEmpty(repositoryUrl) && Strings.notEmpty(groupId) && Strings.notEmpty(artifactId) && Strings.notEmpty(extension);
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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

}
