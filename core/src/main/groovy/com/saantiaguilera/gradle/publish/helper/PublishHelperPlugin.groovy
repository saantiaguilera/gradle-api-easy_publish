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

    public static final String PUBLISH_TASK = 'publishModules'

    public PublishGlobalConfigurations globalConfigurations

    public String rootProjectName

    @Override
    void apply(Project project) {
        rootProjectName = project.name

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

                        pom {
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
                                url configHelper.url
                            }

                            whenConfigured {
                                dependencies.each {
                                    if (it.version == 'undefined' &&
                                            it.groupId == rootProjectName) {
                                        it.version = configHelper.version
                                        it.groupId = configHelper.group

                                        if (configHelper.getLocalArtifact(it.artifactId)) {
                                            it.artifactId = configHelper.getLocalArtifact(it.artifactId)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        proj.task(PUBLISH_TASK) {
            description 'Publishes a new release version of the modules to Bintray.'
            finalizedBy 'bintrayUpload'
            doLast {
                proj.group = configHelper.group
                proj.version = configHelper.version

                proj.bintrayUpload {
                    repoName = configHelper.bintrayRepositoryName
                    packageVcsUrl =
                            "${configHelper.url}/releases/tag/v${proj.version}"
                    versionVcsTag = "v${proj.version}"
                    user = configHelper.bintrayUser
                    apiKey = configHelper.bintrayApiKey
                    dryRun = false
                    publish = true
                    configurations = ['archives']
                    packageName = "${configHelper.group}.${configHelper.artifact}"
                    packageIssueTrackerUrl = "${configHelper.url}/issues"
                    packageWebsiteUrl = configHelper.url
                    versionName = "${proj.version}"
                    packagePublicDownloadNumbers = false
                    packageLicenses = [ configHelper.licenseName ]
                }

                proj.pom {
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
                        url configHelper.url
                    }

                    dependencies.each {
                        if (it.version == 'undefined' &&
                                it.groupId == rootProjectName) {
                            it.version = configHelper.version
                            it.groupId = configHelper.group

                            if (configHelper.getLocalArtifact(it.artifactId)) {
                                it.artifactId = configHelper.getLocalArtifact(it.artifactId)
                            }
                        }
                    }
                }.writeTo("build/poms/pom-default.xml")

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
        ConfigurationHelper configHelper = new ConfigurationHelper(globalConfigurations, project)

        def sourcesJarTask = project.tasks.create "sourcesJar", Jar
        sourcesJarTask.dependsOn project.tasks.getByName("compileJava")
        sourcesJarTask.classifier = 'sources'
        sourcesJarTask.from project.tasks.getByName("compileJava").source

        project.tasks.publishModules.dependsOn 'assemble', 'test', 'check', 'sourcesJar'
    }

    def configureAndroid(Project project) {
        ConfigurationHelper configHelper = new ConfigurationHelper(globalConfigurations, project)

        project.apply plugin: 'com.github.dcendents.android-maven'

        project.android.libraryVariants.all { variant ->
            def sourcesJarTask = project.tasks.create "${variant.buildType.name}SourcesJar", Jar
            sourcesJarTask.dependsOn variant.javaCompile
            sourcesJarTask.classifier = 'sources'
            sourcesJarTask.from variant.javaCompile.source
        }

        project.tasks.publishModules.dependsOn 'assembleRelease', 'testReleaseUnitTest', 'check', 'releaseSourcesJar'
    }

    def addArchives(String packagingType,
                    Project project) {
        ConfigurationHelper configHelper = new ConfigurationHelper(globalConfigurations, project)
        switch (packagingType) {
            case TYPE_JAR:
                def jarParentDirectory = "$project.buildDir/libs/"
                def actualDestination = jarParentDirectory + "${configHelper.artifact}.jar"

                def prevFile = project.file(jarParentDirectory + "${project.name}.jar");
                if (prevFile.exists()) {
                    prevFile.renameTo(actualDestination)
                }

                project.artifacts.add('archives', project.file(actualDestination))
                project.artifacts.add('archives', project.tasks['sourcesJar'])
                break;
            case TYPE_AAR:
                def aarParentDirectory = "$project.buildDir/outputs/aar/"

                def prevFile = project.file(aarParentDirectory + "${project.name}.aar");
                if (prevFile.exists()) {
                    prevFile.delete();
                }

                File aarFile = project.file(aarParentDirectory + "${project.name}-release.aar")

                def actualDestination = aarParentDirectory + "${configHelper.artifact}.aar"

                aarFile.renameTo(actualDestination)

                project.artifacts.add('archives', project.file(actualDestination))
                project.artifacts.add('archives', project.tasks['releaseSourcesJar'])
                break;
        }

        if (project.tasks.findByName('javadocJar')) {
            project.artifacts.add('archives', project.tasks.javadocJar)
        }
    }

}
