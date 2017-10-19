package com.saantiaguilera.gradle.publish.helper

import com.saantiaguilera.gradle.publish.helper.extension.PublishConfigurations
import com.saantiaguilera.gradle.publish.helper.extension.PublishGlobalConfigurations
import com.saantiaguilera.gradle.publish.helper.plugin.AndroidPublisher
import com.saantiaguilera.gradle.publish.helper.plugin.JavaPublisher
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * Created by saantiaguilera on 6/22/17.
 */
class PublishHelperPlugin implements Plugin<Project> {

    public static final String EXTENSION_PUBLISH_CONFIGURATIONS = "publishConfigurations"
    public static final String EXTENSION_PUBLISH_GLOBAL_CONFIGURATIONS = "publishGlobalConfigurations"

    public static final String ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"

    public PublishGlobalConfigurations globalConfigurations

    @Override
    void apply(Project project) {
        project.extensions.create(EXTENSION_PUBLISH_GLOBAL_CONFIGURATIONS, PublishGlobalConfigurations)
        globalConfigurations = project.publishGlobalConfigurations

        project.subprojects { subproject ->
            subproject.extensions.create(EXTENSION_PUBLISH_CONFIGURATIONS, PublishConfigurations)

            plugins.withType(JavaPlugin) {
                subproject.apply plugin: JavaPublisher
            }

            plugins.withId(ANDROID_LIBRARY_PLUGIN_ID) {
                subproject.apply plugin: AndroidPublisher
            }
        }
    }

}
