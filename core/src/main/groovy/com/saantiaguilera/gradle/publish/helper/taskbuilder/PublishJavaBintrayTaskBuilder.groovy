package com.saantiaguilera.gradle.publish.helper.taskbuilder

import org.gradle.api.publish.maven.MavenPublication

class PublishJavaBintrayTaskBuilder extends PublishTaskBuilder {

    PublishJavaBintrayTaskBuilder(variant) {
        super(variant)
    }

    @Override
    protected getArtifact() {
        return project.tasks.jar
    }

    @Override
    protected String getArtifactTaskName() {
        return "jar"
    }

    @Override
    protected void attachPom(MavenPublication it) {
        it.from project.components.java

        // Currently the pom generation is broken since:
        // 1. It doesnt add the dependencies in android
        // 2. It doesnt add the task as dependency of the maven-publication
        // So we have to here add the dependency manually. We dont do it in android projects
        // since we generate the pom manually and write it were it belongs (we simply duplicate
        // the logic of this task but with the issue fixed)
        project.tasks.whenTaskAdded {
            if (it.name.contains('generatePomFileFor')) {
                String hookedTask = it.name.replaceFirst('generatePomFileFor', '').replaceFirst("Publication", '')

                if (hookedTask != null && hookedTask.length() != 0) {
                    hookedTask = (Character.toLowerCase(hookedTask.charAt(0)) as String) + hookedTask.substring(1)
                    project.tasks.findByName(hookedTask).dependsOn it
                }
            }
        }
    }

}
