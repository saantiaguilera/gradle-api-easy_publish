package com.saantiaguilera.gradle.publish.helper.taskbuilder

import com.saantiaguilera.gradle.publish.helper.extension.ConfigurationHelper
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publication.maven.internal.pom.DefaultMavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by saguilera on 10/18/17.
 */
abstract class PublishTaskBuilder {

    public static final String TASK_NAME_PREFIX = "publishModule"
    public static final String TASK_GROUP = "publishing"

    protected Project project
    protected def variant

    protected ConfigurationHelper configHelper

    PublishTaskBuilder(variant) {
        this.variant = variant
    }

    Task build(Project project) {
        this.project = project
        this.configHelper = new ConfigurationHelper(project)

        Task task = project.task ("${TASK_NAME_PREFIX}${variant.name.capitalize()}") {
            doFirst {
                project.tasks.bintrayUpload.with {
                    repoName = configHelper.bintrayRepositoryName

                    packageName = "${project.group}.${project.name}"
                    packageDesc = configHelper.packageDescription
                    packageIssueTrackerUrl = "${configHelper.githubUrl}/issues"
                    packageWebsiteUrl = configHelper.websiteUrl
                    packagePublicDownloadNumbers = configHelper.isPublicDownloadNumbers()
                    packageLicenses = [configHelper.licenseName]
                    packageLabels = configHelper.packageLabels
                    packageVcsUrl = "${configHelper.githubUrl}/releases/tag/v${project.version}"

                    user = configHelper.bintrayUser
                    apiKey = configHelper.bintrayApiKey

                    versionName = "${project.version}"
                    versionVcsTag = "v${project.version}"
                    versionDesc = configHelper.versionDescription

                    signVersion = configHelper.isGpgSign()
                    gpgPassphrase = configHelper.gpgPassphrase

                    userOrg = configHelper.userOrg

                    syncToMavenCentral = configHelper.isSyncableToMavenCentral()
                    ossUser = configHelper.ossUser
                    ossPassword = configHelper.ossPassword
                    ossCloseRepo = configHelper.ossClose

                    dryRun = false
                    publish = true
                    override = configHelper.isOverride()

                    publications = ["${TASK_NAME_PREFIX}${variant.name.capitalize()}"]
                }
            }

            group = TASK_GROUP
            dependsOn getArtifactTaskName(), "${variant.name}SourcesJar"
            finalizedBy 'bintrayUpload'
        }

        createPublication()

        return task
    }

    protected void createPublication() {
        boolean isPureJava = project.plugins.findPlugin(JavaPlugin)
        def sourceDirs = isPureJava ? variant.allSource : variant.sourceSets.collect {
            it.javaDirectories
        }

        def sourcesJar = project.tasks.findByName("${variant.name}SourcesJar")
        if (!sourcesJar) {
            sourcesJar = project.task("${variant.name}SourcesJar", type: Jar) {
                description "Puts sources for ${variant.name} in a jar."
                from sourceDirs
                classifier = 'sources'
            }
        }

        def createMavenPublication = {
            project.publishing.publications {
                "${TASK_NAME_PREFIX}${variant.name.capitalize()}"(MavenPublication) {
                    attachPom(it)
                    artifacts = [
                            getArtifact(),
                            sourcesJar
                    ]
                }
            }
        }

        // I should investigate further as to why a java plugin needs to create the maven
        // publication after evaluation, but an android project doesnt.
        if (project.plugins.hasPlugin(JavaPlugin)) {
            project.afterEvaluate createMavenPublication
        } else {
            createMavenPublication()
        }
    }

    protected abstract def getArtifact()
    protected abstract String getArtifactTaskName()

    protected abstract void attachPom(MavenPublication it)

}