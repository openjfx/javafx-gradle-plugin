/*
 * Copyright (c) 2018, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjfx.gradle.tasks;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.application.CreateStartScripts;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class StartScriptsTask extends JavaFXTask {

    private CreateStartScripts startScripts;

    @Inject
    public StartScriptsTask(Project project) {
        super(project);

        project.getPluginManager().withPlugin(ApplicationPlugin.APPLICATION_PLUGIN_NAME, e -> {
            startScripts = (CreateStartScripts) project.getTasks().findByName(ApplicationPlugin.TASK_START_SCRIPTS_NAME);
            if (startScripts == null) {
                throw new GradleException("StartScripts task not found.");
            }

            startScripts.dependsOn(this);
        });
    }

    @Override
    public FileCollection getClasspath() {
        return startScripts.getClasspath();
    }

    @Override
    public void setTargetClasspath(FileCollection classpath) {
        startScripts.setClasspath(classpath);
    }

    @Override
    public void setTargetJvmArgs(List<String> jvmArgs) {
        startScripts.setDefaultJvmOpts(jvmArgs);
    }

    @Override
    public List<String> getTargetJvmArgs() {
        final List<String> args = new ArrayList<>();
        startScripts.getDefaultJvmOpts().forEach(args::add);
        return args;
    }
}
