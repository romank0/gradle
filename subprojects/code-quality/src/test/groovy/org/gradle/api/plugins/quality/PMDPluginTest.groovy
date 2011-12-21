/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.plugins.quality

import static org.gradle.util.Matchers.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.util.HelperUtil
import org.junit.Test

class PMDPluginTest {
    private final Project project = HelperUtil.createRootProject()
    private final PMDPlugin plugin = new PMDPlugin()
    
    
    @Test
    void apply_appliesReportingBasePlugin() {
        plugin.apply(project)
        assert project.plugins.hasPlugin(ReportingBasePlugin)
    }
    
    @Test
    void apply_addsExtensionObjectsToProject() {
        plugin.apply(project)
        assert project.extensions.pmd instanceof PMDExtension
    }
    
    @Test
    void apply_createsTasksAndAppliesMappingsForEachJavaSourceSet() {
        plugin.apply(project)

        project.plugins.apply(JavaPlugin)
        project.sourceSets.add('custom')
        verifyTaskForSet('main')
        verifyTaskForSet('test')
        verifyTaskForSet('custom')
    }
    
    private void verifyTaskForSet(String setName) {
        def taskSet = setName.substring(0, 1).toUpperCase() + setName.substring(1)
        def task = project.tasks[PMDPlugin.PMD_TASK_NAME + taskSet]
        assert task instanceof PMD
        assert task.defaultSource == project.sourceSets[setName].allJava
        assert task.resultsFile == project.file("build/pmd/${setName}.xml")
        assert task.reportsFile == project.file("build/reports/pmd/${setName}.html")
        
        assertThat(project.tasks[JavaBasePlugin.CHECK_TASK_NAME], dependsOn(hasItem(task.name)))
    }
    
    @Test
    void add_configuresAdditionalTasksDefinedByTheBuildScript() {
        plugin.apply(project)
        
        def task = project.tasks.add('customPMD', PMD)
        assert task.defaultSource == null
        assert task.resultsFile == null
    }
}
