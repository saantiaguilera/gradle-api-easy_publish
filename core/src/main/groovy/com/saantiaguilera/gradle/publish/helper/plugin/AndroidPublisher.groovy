package com.saantiaguilera.gradle.publish.helper.plugin

import com.saantiaguilera.gradle.publish.helper.taskbuilder.PublishAndroidBintrayTaskBuilder
import com.saantiaguilera.gradle.publish.helper.taskbuilder.PublishTaskBuilder
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Android library publishing plugin. It will create a publishing task for each available variant and flavor.
 */
class AndroidPublisher extends Publisher {

    @Override
    void apply(Project project) {
        super.apply(project)

        project.apply plugin: 'com.github.dcendents.android-maven'

        project.android.libraryVariants.all {
            Task task = new PublishAndroidBintrayTaskBuilder(it).build(project)

            if (task.name.toLowerCase().contains("release")) {
                project.task (PublishTaskBuilder.TASK_NAME_PREFIX) {
                    dependsOn task
                    group PublishTaskBuilder.TASK_GROUP
                }
            }
        }


    }

}