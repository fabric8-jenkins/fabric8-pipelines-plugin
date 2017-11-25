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
package org.jenkinsci.plugins.pipelinedsl;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.CpsThread;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class PipelineDSLGlobal extends GlobalVariable {

    public abstract String getFunctionName();

    @Override
    public String getName() {
        return getFunctionName();
    }


    @Override
    public Object getValue(CpsScript script) throws Exception {
        Binding binding = script.getBinding();

        CpsThread c = CpsThread.current();
        if (c == null) {
            throw new IllegalStateException("Expected to be called from CpsThread");
        }

        ClassLoader cl = getClass().getClassLoader();

/*
        Object fabric8Utils = loadFunction(binding, c, cl, "fabric8Utils");
        Object fabric8Commands = loadFunction(binding, c, cl, "fabric8Commands");
*/
        Object function = loadFunction(binding, c, cl, getFunctionName());
        return function;
    }

    private Object loadFunction(Binding binding, CpsThread c, ClassLoader cl, String functionName) throws IOException, InstantiationException, IllegalAccessException {
        String fileName = functionName + ".groovy";
        String scriptPath = "dsl/" + fileName;
        try (Reader r = new InputStreamReader(cl.getResourceAsStream(scriptPath), "UTF-8")) {
            GroovyCodeSource gsc = new GroovyCodeSource(r, fileName, cl.getResource(scriptPath).getFile());
            gsc.setCachable(true);

            Object pipelineDSL = c.getExecution()
                    .getShell()
                    .getClassLoader()
                    .parseClass(gsc)
                    .newInstance();
            binding.setVariable(functionName, pipelineDSL);

/*
            // TODO better place to get the env vars from?
            Map<String, String> envVars = new HashMap<>(System.getenv());

            if (pipelineDSL instanceof Script) {
                Script dslScript = (Script) pipelineDSL;
                System.out.println("\n\n==== DSL is a script!");


                Binding dslBinding = dslScript.getBinding();


                Object env = null;
                try {
                    env = dslBinding.getProperty("env");
                } catch (Exception e) {
                    // ignore
                }
                if (env == null) {
                    System.out.println("\n\n==== DSL has no env property!");
                    dslBinding.setProperty("env", envVars);
                    System.out.println("\n\n==== env property set!");
                } else {
                    System.out.println("==== DSL has an env property");
                }

            } else {
                System.out.println("\n\n==== DSL is NOT a script!");
            }

*/
            return pipelineDSL;
        }
    }

}
