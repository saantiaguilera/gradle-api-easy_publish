package com.saantiaguilera.gradle.publish.helper.taskbuilder

import org.gradle.api.publication.maven.internal.pom.DefaultMavenPom
import org.gradle.api.publish.maven.MavenPublication

class PublishAndroidBintrayTaskBuilder extends PublishTaskBuilder {

    PublishAndroidBintrayTaskBuilder(variant) {
        super(variant)
    }

    @Override
    protected getArtifact() {
        return "$project.buildDir/outputs/aar/${project.getName()}-${variant.name}.aar"
    }

    @Override
    protected String getArtifactTaskName() {
        return "bundle${variant.name.capitalize()}"
    }

    @Override
    protected void attachPom(MavenPublication publication) {
        // We simulate the generatePomForMavenPublication task since the task is broken
        // as it doesnt resolve dependencies of an android library
        publication.pom {
            DefaultMavenPom createdPom = null
            project.install {
                repositories.mavenInstaller {
                    createdPom = pom {
                        project {
                            groupId project.group
                            artifactId project.name
                            version project.version

                            packaging 'aar'

                            if (configHelper.websiteUrl) {
                                url configHelper.websiteUrl
                            }

                            if (configHelper.licenseName && configHelper.licenseUrl) {
                                licenses {
                                    license {
                                        name configHelper.licenseName
                                        url configHelper.licenseUrl
                                    }
                                }
                            }
                        }
                    }
                }
            }
            createdPom.writeTo("${project.buildDir}/" +
                    "publications/" +
                    "${TASK_NAME_PREFIX}${variant.name.capitalize()}/" +
                    "pom-default.xml")
        }
    }

}
