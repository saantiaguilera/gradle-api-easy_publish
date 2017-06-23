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

    public static final String TYPE_JAR = 'jar'
    public static final String TYPE_AAR = 'aar'

    public static final String ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"

    public static final String PUBLISH_TASK = 'publishModules'

    @Override
    void apply(Project project) {
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

        proj.afterEvaluate {
            proj.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: "file://${System.properties['user.home']}/.m2/repository")

                        pom {
                            version = proj.publishConfigurations.versionName
                            artifactId = proj.publishConfigurations.artifactId
                            groupId = proj.publishConfigurations.groupId
                            packaging = packagingType

                            project {
                                licenses {
                                    license {
                                        name proj.publishConfigurations.licenseName
                                        url proj.publishConfigurations.licenseUrl
                                        distribution 'repo'
                                    }
                                }
                                packaging packagingType
                                url proj.publishConfigurations.url
                            }

                            whenConfigured {
                                dependencies.each {
                                    if (it.version == 'undefined' &&
                                            proj.publishConfigurations.localArtifacts &&
                                            proj.publishConfigurations.localArtifacts.contains(it.artifactId)) {
                                        it.version = proj.publishConfigurations.versionName
                                        it.groupId = proj.publishConfigurations.groupId
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
                proj.group = proj.publishConfigurations.groupId
                proj.version = proj.publishConfigurations.versionName

                proj.bintrayUpload {
                    repoName = proj.publishConfigurations.bintrayRepository ?: 'maven'
                    packageVcsUrl =
                            "${proj.publishConfigurations.url}/releases/tag/v${proj.version}"
                    versionVcsTag = "v${proj.version}"
                    user = proj.publishConfigurations.bintrayUser
                    apiKey = proj.publishConfigurations.bintrayApiKey
                    dryRun = false
                    publish = true
                    configurations = ['archives']
                    packageName = "${proj.publishConfigurations.groupId}.${proj.publishConfigurations.artifactId}"
                    packageIssueTrackerUrl = "${proj.publishConfigurations.url}/issues"
                    packageWebsiteUrl = proj.publishConfigurations.url
                    versionName = "${proj.version}"
                    packagePublicDownloadNumbers = false
                }

                proj.pom {
                    version = proj.publishConfigurations.versionName
                    artifactId = proj.publishConfigurations.artifactId
                    groupId = proj.publishConfigurations.groupId
                    packaging = packagingType

                    project {
                        licenses {
                            license {
                                name proj.publishConfigurations.licenseName
                                url proj.publishConfigurations.licenseUrl
                                distribution 'repo'
                            }
                        }
                        packaging packagingType
                        url proj.publishConfigurations.url
                    }

                    dependencies.each {
                        if (it.version == 'undefined' &&
                                proj.publishConfigurations.localArtifacts &&
                                proj.publishConfigurations.localArtifacts.contains(it.artifactId)) {
                            it.version = proj.publishConfigurations.versionName
                            it.groupId = proj.publishConfigurations.groupId
                        }
                    }
                }.writeTo("build/poms/pom-default.xml")

                proj.file("$proj.buildDir/libs/${proj.name}-sources.jar")
                        .renameTo("$proj.buildDir/libs/${proj.name}-${proj.version}-sources.jar")

                proj.configurations.archives.artifacts.clear()

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

        project.tasks.publishModules.dependsOn 'assemble', 'test', 'check', 'sourcesJar'

        project.task("${PUBLISH_TASK}AddArtifacts") {
            mustRunAfter PUBLISH_TASK
            doLast {
                def jarParentDirectory = "$project.buildDir/libs/"
                def actualDestination = jarParentDirectory + "${project.publishConfigurations.artifactId}.jar"

                def prevFile = project.file(jarParentDirectory + "${project.name}.jar");
                if (prevFile.exists()) {
                    prevFile.renameTo(actualDestination)
                }

                project.artifacts.add('archives', project.file(actualDestination))
                project.artifacts.add('archives', project.tasks['sourcesJar'])
            }
        }
    }

    def configureAndroid(Project project) {
        project.apply plugin: 'com.github.dcendents.android-maven'

        project.android.libraryVariants.all { variant ->
            def sourcesJarTask = project.tasks.create "${variant.buildType.name}SourcesJar", Jar
            sourcesJarTask.dependsOn variant.javaCompile
            sourcesJarTask.classifier = 'sources'
            sourcesJarTask.from variant.javaCompile.source
        }

        project.tasks.publishModules.dependsOn 'assembleRelease', 'testReleaseUnitTest', 'check', 'releaseSourcesJar'

        project.task("${PUBLISH_TASK}AddArtifacts") {
            mustRunAfter PUBLISH_TASK
            doLast {
                def aarParentDirectory = "$project.buildDir/outputs/aar/"

                def prevFile = project.file(aarParentDirectory + "${project.name}.aar");
                if (prevFile.exists()) {
                    prevFile.delete();
                }

                File aarFile = project.file(aarParentDirectory + "${project.name}-release.aar")

                def actualDestination = aarParentDirectory + "${project.publishConfigurations.artifactId}.aar"

                aarFile.renameTo(actualDestination)

                project.artifacts.add('archives', project.file(actualDestination))
                project.artifacts.add('archives', project.tasks['releaseSourcesJar'])
            }
        }
    }

}
