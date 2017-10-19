package com.saantiaguilera.gradle.publish.helper.extension

/**
 * Example:
 *
 publishConfigurations {
    groupId = project.groupId
    artifactId = 'core'
    versionName = libraryVersion

    localArtifacts = [ "annotation" ] // Name of a local dependency this module consumes

    bintrayRepository = 'maven'

    url = "https://github.com/saantiaguilera/android-api-SecureKeys"

    bintrayUser = System.getenv(BINTRAY_USER) // Or get it from a file?
    bintrayApiKey = System.getenv(BINTRAY_APIKEY) // Or get if from a file?
 }
 *
 * Created by saguilera on 6/22/17.
 */
class PublishConfigurations {

    String bintrayRepository                 // defaults to "maven"

    boolean publicDownloadNumbers            // Defaults to false. Download numbers are visible to all.
    boolean override                         // Defaults to false. Overrides artifacts already published for a version

    String githubUrl                         // github url
    String websiteUrl                        // website url (defaults to github url if none)

    String bintrayApiKey                     // Api key of bintray
    String bintrayUser                       // User of bintray

    String licenseUrl                        // License url to find it
    String licenseName                       // License full name

    String packageDescription                // Package description
    String versionDescription                // Version description

    List<String> packageLabels               // Labels for finding via maven the repository

    boolean gpgSign                          // If should sign it with gpg. Default false
    String gpgPassphrase                     // Gpg passphrase if gpgSign == true

    String userOrg                           // Organization name

    boolean syncToMavenCentral               // Defaults to false. If true, below fields must be setted
    String ossUser                           // OSS user
    String ossPassword                       // OSS password
    String ossClose                          // By default ('1') the staging repository is closed and artifacts are
                                             // released to Maven Central.
                                             // You can optionally turn this behaviour off (by puting '0' as value)
                                             // and release the version manually


}
