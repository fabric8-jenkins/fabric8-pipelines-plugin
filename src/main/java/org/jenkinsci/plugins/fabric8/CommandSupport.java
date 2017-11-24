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
package org.jenkinsci.plugins.fabric8;

import io.fabric8.utils.IOHelpers;
import org.jenkinsci.plugins.workflow.cps.EnvActionImpl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 */
public abstract class CommandSupport implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Logger logger = Logger.getInstance();
    private transient Map<String, String> env = createEnv();
    private transient ShellFacade shellFacade;
    private File currentDir = new File(".");

    public CommandSupport() {
    }

    public CommandSupport(CommandSupport parent) {
        setLogger(parent.getLogger());
        setShellFacade(parent.getShellFacade());
        setCurrentDir(parent.getCurrentDir());
    }


    public void echo(String message) {
        getLogger().info(message);
    }

    public void warning(String message) {
        echo("WARNING: " + message);
    }

    public void error(String message) {
        getLogger().error(message);
    }

    public void error(String message, Throwable t) {
        getLogger().error(message, t);
    }

    public void updateEnvironment(Object envVar) {
        //echo("===== updateEnvironment passed in " + envVar + " " + (envVar != null ? envVar.getClass().getName() : ""));
        if (envVar instanceof EnvActionImpl) {
            EnvActionImpl envAction = (EnvActionImpl) envVar;
            Map<String, String> map = envAction.getOverriddenEnvironment();
            setEnv(map);
        } else if (envVar instanceof Map) {
            setEnv((Map<String, String>) envVar);
        }
    }

    public void updateSh(Object value) {
        echo("===== updateSh passed in " + value + " " + (value != null ? value.getClass().getName() : ""));
    }

    public String getenv(String name) {
        return getEnv().get(name);
    }

    /**
     * Invokes the given command
     */
    public void sh(String command) {
        ShellFacade shell = getShellFacade();
        if (shell == null) {
            throw new IllegalArgumentException("No shellFacade has been injected into " + this + " so cannot invoke sh(" + command + ")");
        }
        shell.apply(command, false);
    }

    /**
     * Returns the output of the given command
     */
    public String shOutput(String command) {
        ShellFacade shell = getShellFacade();
        if (shell == null) {
            throw new IllegalArgumentException("No shellFacade has been injected into " + this + " so cannot invoke sh(" + command + ")");
        }
        return shell.apply(command, false).trim();
    }


    // Properties
    //-------------------------------------------------------------------------

    public File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getInstance();
        }
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Map<String, String> getEnv() {
        if (env == null) {
            env = createEnv();
        }
        return env;
    }

    public void setEnv(Map<String, String> env) {
        if (env != null) {
            if (env.isEmpty()) {
                warning("setting the environment to an empty map");
            } else {
                echo("setting the environment to: " + env);
            }
            this.env = env;
        } else {
            error("No environment map is specified");
        }
    }

    public ShellFacade getShellFacade() {
        return shellFacade;
    }

    public void setShellFacade(ShellFacade shellFacade) {
        this.shellFacade = shellFacade;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected String readFile(String name) throws IOException {
        return IOHelpers.readFully(createFile(name));
    }

    protected File createFile(String name) {
        return new File(getCurrentDir(), name);
    }

    protected void hubotSend(String message, String room, boolean failOnError) {
        throw new FailedBuildException("TODO");
    }

    protected HashMap<String, String> createEnv() {
        return new HashMap<>();
    }


}
