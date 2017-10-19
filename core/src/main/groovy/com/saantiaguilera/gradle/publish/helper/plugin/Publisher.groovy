package com.saantiaguilera.gradle.publish.helper.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

/**
 * Abstract publisher plugin, it will configure the project for having publishing features.
 */
abstract class Publisher implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.with {
            apply plugin: MavenPublishPlugin
            apply plugin: MavenPlugin
            apply plugin: 'com.jfrog.bintray'

            configurations {
                archives {
                    extendsFrom project.configurations.default
                }
            }
        }
    }

}