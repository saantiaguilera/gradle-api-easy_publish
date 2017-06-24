package com.saantiaguilera.gradle.publish.helper

public class PublishGlobalConfigurations {

    String groupId                           // Group
    String versionName                       // Version

    String bintrayRepository                 // defaults to "maven"

    Map<String, String> artifactsMappings    // Local mappings of moduleName -> artifactName

    String url                               // github url

    String bintrayApiKey                     // Api key of bintray
    String bintrayUser                       // User of bintray

    String licenseUrl                        // License url to find it
    String licenseName                       // License full name

    List<String> publishOrder                // Order for publishing modules

}