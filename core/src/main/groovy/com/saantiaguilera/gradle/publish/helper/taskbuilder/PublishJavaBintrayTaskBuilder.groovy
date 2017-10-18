package com.saantiaguilera.gradle.publish.helper.taskbuilder

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
    protected getArtifactTaskName() {
        return "jar"
    }

}
