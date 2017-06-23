package com.saantiaguilera.gradle.publish.helper

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
public class PublishConfigurations {

    String groupId              // Group
    String artifactId           // Artifact
    String versionName          // Version

    List<String> localArtifacts // Local artifacts if you have local dependencies declared (compile (":my_module"))

    String bintrayRepository    // defaults to "maven"

    String url                  // github url

    String bintrayApiKey        // Api key of bintray
    String bintrayUser          // User of bintray

    String licenseUrl           // License url to find it
    String licenseName          // License full name

}
