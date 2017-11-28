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
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;

import java.io.IOException;

@Extension
public class Fabric8CommandsDSL extends PipelineDSLGlobal {

    @Override
    public String getFunctionName() {
        return "fabric8Commands";
    }

    @Extension
    public static class MiscWhitelist extends ProxyWhitelist {
        public MiscWhitelist() throws IOException {
            super(new StaticWhitelist(
                    // for exposing sh()
                    "method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object",

                    // for println
                    "staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods println java.lang.Object java.lang.Object",

                    // finding git url
                    "new java.io.File java.lang.String",
                    "new java.io.File java.io.File java.lang.String",
                    "method java.io.File getAbsolutePath",

                    "staticMethod org.jenkinsci.plugins.fabric8.helpers.GitHelper extractGitUrl java.lang.String",
                    "staticMethod org.jenkinsci.plugins.fabric8.helpers.GitHelper parseGitRepositoryInfo java.lang.String",
                    "method org.jenkinsci.plugins.fabric8.helpers.GitRepositoryInfo *",
                    "method org.jenkinsci.plugins.fabric8.helpers.GitRepositoryInfo * *",

                    // string utils
                    "staticMethod io.fabric8.utils.Strings isNotBlank java.lang.String",
                    "staticMethod io.fabric8.utils.Strings isNullOrBlank java.lang.String",
                    "staticMethod io.fabric8.utils.Strings notEmpty java.lang.String",

                    "method java.util.Map$Entry getKey",
                    "method java.util.Map$Entry getValue"
            ));
        }
    }

}
