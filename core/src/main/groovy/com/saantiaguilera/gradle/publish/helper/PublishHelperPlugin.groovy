package com.saantiaguilera.gradle.publish.helper

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by saantiaguilera on 6/22/17.
 */
public class PublishHelperPlugin implements Plugin<Project> {

    public static final String EXTENSION_PUBLISH_CONFIGURATIONS = "publishConfigurations"
    public static final String EXTENSION_PUBLISH_GLOBAL_CONFIGURATIONS = "publishGlobalConfigurations"

    public static final String TYPE_JAR = 'jar'
    public static final String TYPE_AAR = 'aar'

    public static final String ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"

    public static final String PUBLISH_ROOT_TASK = 'publishModules'
    public static final String PUBLISH_MODULE_TASK = 'publishModule'

    public static final Map<String, List<String>> scopeConfigurations = [
            provided : [
                'compileOnly', 'provided', 'releaseProvided'
            ],
            compile : [
                'compile', 'releaseCompile'
            ],
            test : [
                'testCompile', 'testReleaseCompile'
            ]
    ]

    public PublishGlobalConfigurations globalConfigurations

    public Project rootProject

    @Override
    void apply(Project project) {
        rootProject = project

        project.extensions.create(EXTENSION_PUBLISH_GLOBAL_CONFIGURATIONS, PublishGlobalConfigurations)
        globalConfigurations = project.publishGlobalConfigurations
        project.subprojects { subproject ->
            subproject.extensions.create(EXTENSION_PUBLISH_CONFIGURATIONS, PublishConfigurations)

            subproject.afterEvaluate {
                subproject.configurations {
                    archives {
                        extendsFrom subproject.configurations.default
                    }
                }
            }

            plugins.withType(JavaPlugin) {
                configure(subproject, TYPE_JAR)
                configureJava(subproject)
            }

            plugins.withId(ANDROID_LIBRARY_PLUGIN_ID) {
                configure(subproject, TYPE_AAR)
                configureAndroid(subproject)
            }
        }

        project.afterEvaluate {
            project.task(PUBLISH_ROOT_TASK) { task ->
                task.description 'Publish all the possible modules'
                project.subprojects.each { subproject ->
                    if (subproject.tasks.findByName(PUBLISH_MODULE_TASK)) {
                        task.dependsOn subproject.tasks[PUBLISH_MODULE_TASK]
                    }
                }
            }
        }
    }

    def configure(Project proj, String packagingType) {
        proj.apply plugin: 'maven'
        proj.apply plugin: 'com.jfrog.bintray'

        ConfigurationHelper configHelper = new ConfigurationHelper(globalConfigurations, proj)

        proj.afterEvaluate {
            proj.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: "file://${System.properties['user.home']}/.m2/repository")
                        pom(flattenPom(proj, packagingType))
                            .writeTo("build/poms/pom-default.xml")


                    }
                }
            }
        }

        proj.task(PUBLISH_MODULE_TASK) {
            description 'Publishes a new release version of the module to Bintray.'
            finalizedBy 'bintrayUpload'
            doLast {
                proj.group = configHelper.group
                proj.version = configHelper.version

                proj.bintrayUpload {
                    repoName = configHelper.bintrayRepositoryName

                    packageName = "${configHelper.group}.${configHelper.artifact}"
                    packageDesc = configHelper.packageDescription
                    packageIssueTrackerUrl = "${configHelper.githubUrl}/issues"
                    packageWebsiteUrl = configHelper.websiteUrl
                    packagePublicDownloadNumbers = configHelper.isPublicDownloadNumbers()
                    packageGithubRepo = configHelper.githubUrl
                    packageLicenses = [configHelper.licenseName]
                    packageLabels = configHelper.packageLabels
                    packageVcsUrl = "${configHelper.githubUrl}/releases/tag/v${proj.version}"

                    user = configHelper.bintrayUser
                    apiKey = configHelper.bintrayApiKey

                    versionName = "${proj.version}"
                    versionVcsTag = "v${proj.version}"
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

                    configurations = ['archives']
                }

                proj.file("$proj.buildDir/libs/${proj.name}-sources.jar")
                        .renameTo("$proj.buildDir/libs/${proj.name}-${proj.version}-sources.jar")

                proj.configurations.archives.artifacts.clear()

                addArchives(packagingType, proj)

                println "Publishing: ${String.format("%s:%s:%s", proj.group, proj.name, proj.version)}"
            }
        }

        proj.tasks.uploadArchives.dependsOn.clear()
    }

    def configureJava(Project project) {
        def sourcesJarTask = project.tasks.create "sourcesJar", Jar
        sourcesJarTask.dependsOn project.tasks.getByName("compileJava")
        sourcesJarTask.classifier = 'sources'
        sourcesJarTask.from project.tasks.getByName("compileJava").source

        project.tasks[PUBLISH_MODULE_TASK].dependsOn 'assemble', 'test', 'check', 'sourcesJar'
    }

    def configureAndroid(Project project) {
        project.apply plugin: 'com.github.dcendents.android-maven'

        def sourcesJarTask = project.tasks.create "sourcesJar", Jar
        sourcesJarTask.classifier = 'sources'
        sourcesJarTask.from project.android.sourceSets.main.java.srcDirs

        project.tasks[PUBLISH_MODULE_TASK].dependsOn 'assembleRelease', 'testReleaseUnitTest', 'check', 'sourcesJar'
    }

    def flattenPom(Project proj, String packagingType) {
        ConfigurationHelper configHelper = new ConfigurationHelper(globalConfigurations, proj)
        return {
            version = configHelper.version
            artifactId = configHelper.artifact
            groupId = configHelper.group
            packaging = packagingType

            project {
                licenses {
                    license {
                        name configHelper.licenseName
                        url configHelper.licenseUrl
                        distribution 'repo'
                    }
                }
                packaging packagingType
                url configHelper.githubUrl
            }

            if (!generatedDependencies.isEmpty()) {
                def newGeneratedDependencies = []
                def dependencyGenerator
                dependencyGenerator = generatedDependencies.get(0)

                // Since gradle is not adding some variants (eg releaseCompile or provided), I add them manually.
                scopeConfigurations.each { pomScope, configs ->
                    configs.each { scope ->
                        if (proj.configurations.findByName(scope)) {
                            proj.configurations[scope].allDependencies.each { def mockDependency ->
                                def dependency = dependencyGenerator.clone()
                                if (isLocalDependency(mockDependency.group, mockDependency.version)) {
                                    dependency.groupId = configHelper.group
                                    dependency.version = configHelper.version

                                    if (configHelper.getLocalArtifact(mockDependency.name)) {
                                        dependency.artifactId = configHelper.getLocalArtifact(mockDependency.name)
                                    } else {
                                        dependency.artifactId = mockDependency.name
                                    }
                                } else {
                                    dependency.groupId = mockDependency.group
                                    dependency.artifactId = mockDependency.name
                                    dependency.version = mockDependency.version
                                }
                                dependency.scope = pomScope
                                newGeneratedDependencies.add(dependency)
                            }
                        }
                    }
                }

                configurations = null
                dependencies = newGeneratedDependencies
            }
        }
    }

    def addArchives(String packagingType,
                    Project project) {
        ConfigurationHelper configHelper = new ConfigurationHelper(globalConfigurations, project)
        switch (packagingType) {
            case TYPE_JAR:
                def jarParentDirectory = "$project.buildDir/libs/"
                def prevFile = project.file(jarParentDirectory + "${project.name}.jar");
                def actualFile = project.file(jarParentDirectory + "${configHelper.artifact}-${configHelper.version}.jar")

                if (prevFile.exists() && prevFile.path != actualFile.path) {
                    if (actualFile.exists()) {
                        actualFile.delete()
                    }
                    actualFile << prevFile.bytes
                }

                project.artifacts.add('archives', actualFile)
                break;
            case TYPE_AAR:
                def aarParentDirectory = "$project.buildDir/outputs/aar/"
                File aarFile = project.file(aarParentDirectory + "${project.name}-release.aar")
                File actualFile = project.file(aarParentDirectory + "${configHelper.artifact}.aar")

                if (aarFile.exists() != aarFile.path != actualFile.path) {
                    if (actualFile.exists()) {
                        actualFile.delete()
                    }
                    actualFile << aarFile.bytes
                }

                project.artifacts.add('archives', actualFile)
                break;
        }

        project.artifacts.add('archives', project.tasks['sourcesJar'])
        if (project.tasks.findByName('javadocJar')) {
            project.artifacts.add('archives', project.tasks.javadocJar)
        }
    }

    def isLocalDependency(def groupId, def version) {
        return (version == 'unspecified' || version == 'undefined') &&
                groupId == rootProject.name
    }

}
