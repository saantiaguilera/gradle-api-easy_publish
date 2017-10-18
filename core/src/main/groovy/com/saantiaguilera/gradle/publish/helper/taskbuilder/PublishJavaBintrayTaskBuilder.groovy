package com.saantiaguilera.gradle.publish.helper.taskbuilder

import org.gradle.api.publish.maven.MavenPublication

/**
 * Created by saguilera on 10/18/17.
 */
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
        it.from(project.components.java)
    }

}
