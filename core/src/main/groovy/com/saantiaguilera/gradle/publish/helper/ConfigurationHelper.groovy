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
    }

    def getVersion() {
        def version = scopeConf.versionName ?: globalConf.versionName
        if (!version) {
            throw new GradleException('No version found in global or scope extension of module:' + subprojectName)
        }
        return version
    }

    def getGroup() {
        def group = scopeConf.groupId ?: globalConf.groupId
        if (!group) {
            throw new GradleException('No groupId found in global or scope extension of module:' + subprojectName)
        }
        return group
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

    def isPublicDownloadNumbers() {
        return scopeConf.publicDownloadNumbers ?: globalConf.publicDownloadNumbers
    }

    def getGithubUrl() {
        def url = scopeConf.githubUrl ?: globalConf.githubUrl
        if (!url) {
            throw new GradleException('No githubUrl found in global or scope extension of module:' + subprojectName)
        }
        return url
    }

    def getWebsiteUrl() {
        def url = scopeConf.websiteUrl ?: globalConf.websiteUrl
        if (!url) {
            url = githubUrl
        }
        return url
    }

    def getBintrayRepositoryName() {
        def name = scopeConf.bintrayRepository ?: globalConf.bintrayRepository
        return name ?: DEFAULT_REPO_NAME
    }

    def getBintrayUser() {
        def user = scopeConf.bintrayUser ?: globalConf.bintrayUser
        if (!user) {
            throw new GradleException('No bintrayUser found in global or scope extension of module:' + subprojectName)
        }
        return user
    }

    def getBintrayApiKey() {
        def apiKey = scopeConf.bintrayApiKey ?: globalConf.bintrayApiKey
        if (!apiKey) {
            throw new GradleException('No bintrayApiKey found in global or scope extension of module:' + subprojectName)
        }
        return apiKey
    }

    def isOverride() {
        return scopeConf.override ?: globalConf.override
    }

    def getPackageDescription() {
        return scopeConf.packageDescription ?: globalConf.packageDescription
    }

    def getVersionDescription() {
        return scopeConf.versionDescription ?: globalConf.versionDescription
    }

    def getPackageLabels() {
        return scopeConf.packageLabels ?: globalConf.packageLabels
    }

    def isGpgSign() {
        return scopeConf.gpgSign ?: globalConf.gpgSign
    }

    def getGpgPassphrase() {
        return scopeConf.gpgPassphrase ?: globalConf.gpgPassphrase
    }

    def getUserOrg() {
        return scopeConf.userOrg ?: globalConf.userOrg
    }

    def isSyncableToMavenCentral() {
        return scopeConf.syncToMavenCentral ?: globalConf.syncToMavenCentral
    }

    def getOssUser() {
        def user = scopeConf.ossUser ?: globalConf.ossUser
        if (!user && isSyncableToMavenCentral()) {
            throw new GradleException('ossUser property must be declared if using syncToMavenCentral')
        }
        return user
    }

    def getOssPassword() {
        def password = scopeConf.ossPassword ?: globalConf.ossPassword
        if (!password && isSyncableToMavenCentral()) {
            throw new GradleException('ossPassword property must be declared if using syncToMavenCentral')
        }
        return password
    }

    def getOssClose() {
        def close = scopeConf.ossClose ?: globalConf.ossClose
        if (close && (close != '1' && close != '0')) {
            throw new GradleException('ossClose property must be "0" or "1"')
        }
        if (!close && isSyncableToMavenCentral()) {
            close = '1'
        }
        return close
    }

}