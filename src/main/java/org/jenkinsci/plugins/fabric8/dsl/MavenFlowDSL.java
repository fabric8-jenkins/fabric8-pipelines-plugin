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
package org.jenkinsci.plugins.fabric8.dsl;

import hudson.Extension;
import org.jenkinsci.plugins.pipelinedsl.PipelineDSLGlobal;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;

import java.io.IOException;

@Extension
public class MavenFlowDSL extends PipelineDSLGlobal {

    @Override
    public String getFunctionName() {
        return "mavenFlow";
    }

    @Extension
    public static class MiscWhitelist extends ProxyWhitelist {
        public MiscWhitelist() throws IOException {
            super(new StaticWhitelist(
                    "new org.jenkinsci.plugins.fabric8.Fabric8Commands",
                    "new org.jenkinsci.plugins.fabric8.Utils",

                    "method org.jenkinsci.plugins.fabric8.steps.MavenFlow perform org.jenkinsci.plugins.fabric8.Utils",
                    "staticMethod org.jenkinsci.plugins.fabric8.steps.MavenFlow perform org.jenkinsci.plugins.fabric8.Utils",

                    // for exposing sh()
                    "method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object",

                    "method org.jenkinsci.plugins.fabric8.CommandSupport updateEnvironment java.lang.Object",
                    "method org.jenkinsci.plugins.fabric8.CommandSupport setEnv java.util.Map",
                    "method org.jenkinsci.plugins.fabric8.CommandSupport setShellFacade org.jenkinsci.plugins.fabric8.ShellFacade",
                    "method org.jenkinsci.plugins.fabric8.CommandSupport updateSh java.lang.Object",
                    "method org.jenkinsci.plugins.fabric8.CommandSupport *",
                    "method org.jenkinsci.plugins.fabric8.Utils *",

                    "method java.util.Map$Entry getKey",
                    "method java.util.Map$Entry getValue"
            ));
        }
    }

}
