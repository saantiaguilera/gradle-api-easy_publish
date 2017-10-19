package com.saantiaguilera.gradle.publish.helper.plugin

import com.saantiaguilera.gradle.publish.helper.taskbuilder.PublishJavaBintrayTaskBuilder
import com.saantiaguilera.gradle.publish.helper.taskbuilder.PublishTaskBuilder
import org.gradle.api.Project

/**
 * Java publishing plugin. It will create a publish task for each available sourceset.
 */
class JavaPublisher extends Publisher {

    private static final String SOURCE_SETS_TEST = 'test'
    private static final String SOURCE_SETS_DEFAULT = 'main'

    @Override
    void apply(Project project) {
        super.apply(project)

        project.sourceSets.each {
            if (it.name != SOURCE_SETS_TEST) {
                new PublishJavaBintrayTaskBuilder(it).build(project)
            }
        }

        project.task (PublishTaskBuilder.TASK_NAME_PREFIX) {
            group PublishTaskBuilder.TASK_GROUP
            dependsOn project.tasks.find { it.name.contains(PublishTaskBuilder.TASK_NAME_PREFIX) && it.name.toLowerCase().contains(SOURCE_SETS_DEFAULT) }
        }
    }

}