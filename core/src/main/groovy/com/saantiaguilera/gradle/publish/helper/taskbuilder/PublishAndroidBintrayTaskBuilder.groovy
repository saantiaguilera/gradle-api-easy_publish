package com.saantiaguilera.gradle.publish.helper.taskbuilder

/**
 * Created by saguilera on 10/18/17.
 */
class PublishAndroidBintrayTaskBuilder extends PublishTaskBuilder {

    PublishAndroidBintrayTaskBuilder(variant) {
        super(variant)
    }

    @Override
    protected getArtifact() {
        return "$project.buildDir/outputs/aar/${project.getName()}-${variant.name}.aar"
    }

    @Override
    protected getArtifactTaskName() {
        return "bundle${variant.name.capitalize()}"
    }

}
