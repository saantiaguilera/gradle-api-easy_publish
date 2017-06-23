package com.saantiaguilera.gradle.publish.helper

import org.gradle.api.GradleException
import org.gradle.api.Project

public class ConfigurationHelper {

    public static final String DEFAULT_REPO_NAME = 'maven'

    PublishGlobalConfigurations globalConf
    PublishConfigurations scopeConf

    String subprojectName

    ConfigurationHelper(PublishGlobalConfigurations globalConfigurations,
                        Project subproject) {
        this.globalConf = globalConfigurations
        this.scopeConf = subproject.publishConfigurations
        this.subprojectName = subproject.name

        if (!bintrayUser) {
            throw new GradleException('No bintray.user found in global or scope extension of module:' + subprojectName)
        }

        if (!bintrayApiKey) {
            throw new GradleException('No bintray.apikey found in global or scope extension of module:' + subprojectName)
        }

        if (!group) {
            throw new GradleException('No groupId found in global or scope extension of module:' + subprojectName)
        }

        if (!version) {
            throw new GradleException('No version found in global or scope extension of module:' + subprojectName)
        }

        if (!url) {
            throw new GradleException('No url found in global or scope extension of module:' + subprojectName)
        }
    }

    def getVersion() {
        return scopeConf.versionName ?: globalConf.versionName
    }

    def getGroup() {
        return scopeConf.groupId ?: globalConf.groupId
    }

    /**
     * Returns artifactId of the module extension.
     * Else returns artifactId of the mapping in the global extension
     * Else returns the module name
     * @return
     */
    def getArtifact() {
        if (!scopeConf.artifactId) {
            if (globalConf.artifactsMappings) {
                return globalConf.artifactsMappings.get(subprojectName, subprojectName)
            } else {
                return subprojectName
            }
        }

        return scopeConf.artifactId
    }

    def getLocalArtifact(String name) {
        return globalConf.artifactsMappings?.get(name)
    }

    def getLicenseName() {
        return scopeConf.licenseName ?: globalConf.licenseName
    }

    def getLicenseUrl() {
        return scopeConf.licenseUrl ?: globalConf.licenseUrl
    }

    def getUrl() {
        return scopeConf.url ?: globalConf.url
    }

    def getBintrayRepositoryName() {
        def name = scopeConf.bintrayRepository ?: globalConf.bintrayRepository
        return name ?: DEFAULT_REPO_NAME
    }

    def getBintrayUser() {
        return scopeConf.bintrayUser ?: globalConf.bintrayUser
    }

    def getBintrayApiKey() {
        return scopeConf.bintrayApiKey ?: globalConf.bintrayApiKey
    }

}