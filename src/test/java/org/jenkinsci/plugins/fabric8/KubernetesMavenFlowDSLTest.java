/**
 * Copyright (C) Original Authors 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.fabric8;

import jenkins.plugins.git.GitStep;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.scm.GitSampleRepoRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import static org.junit.Assert.assertNotNull;


public class KubernetesMavenFlowDSLTest extends AbstractKubernetesPipelineTest {
/*
    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();
*/

    @Ignore
    public void sampleRepoCIBuild() throws Exception {
        assertBuildSuccess("scripted");
    }

    @Test
    public void sampleRepoCDBuild() throws Exception {
        assertBuildSuccess("pod-template-by-name");
    }

    public void assertBuildSuccess(String branchName) throws Exception {
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "test-" + branchName);

        GitStep gitStep = new GitStep("https://github.com/jstrachan/test-maven-flow-project.git");
        gitStep.setBranch(branchName);
        p.setDefinition(new CpsScmFlowDefinition(gitStep.createSCM(), "Jenkinsfile"));


        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatusSuccess(r.waitForCompletion(b));
        r.assertLogContains("Apache Maven 3.3.9", b);
        //r.assertLogContains("INSIDE_CONTAINER_ENV_VAR = " + CONTAINER_ENV_VAR_VALUE + "\n", b);
    }

}
