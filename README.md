## Easy Publish

[![Build Status](https://travis-ci.org/saantiaguilera/gradle-api-easy_publish.svg?branch=master)](https://travis-ci.org/saantiaguilera/gradle-api-easy_publish) [![Download](https://api.bintray.com/packages/saantiaguilera/maven/com.saantiaguilera.gradle.publish.helper.core/images/download.svg) ](https://bintray.com/saantiaguilera/maven/com.saantiaguilera.gradle.publish.helper.core/_latestVersion)

Publish multiple artifacts (project modules) to bintray in a mega easy way.

This plugin supports JAR / AAR projects.

### Get started

Apply in your root `build.gradle`:

```gradle
apply plugin: 'com.saantiaguilera.gradle.publish.helper'

buildscript {
    // ...
    dependencies {
        classpath "com.saantiaguilera.gradle.publish.helper:core:<latest_version>"
    }
}
```

This will create a single task `publishModules`. Please read the _Run!_ part of the readme for more information

### Configure

This plugin detects automatically a publishable module based on:

* It has `apply plugin: 'java'` -> JAR module
* It has `apply plugin: 'com.android.library` -> AAR module
* It has something else -> Non publishable module

For every module you should provide a `PublishConfigurations` where we get the configurations for the publishing. The extension should look like this:

```gradle
publishConfigurations {
    groupId = 'com.my.library'
    artifactId = 'core'
    versionName = '1.0.0'

    localArtifacts = [ "the_artifact_of_local_module_I_consume" ] // Name of a local dependency this module consumes, in case it has

    bintrayRepository = 'maven' // Defaults to 'maven' if nothing used, but you can specify your own

    url = "https://github.com/saantiaguilera/android-api-SecureKeys" // Your url

    bintrayUser = System.getenv(BINTRAY_USER) // Or get it from a file?
    bintrayApiKey = System.getenv(BINTRAY_APIKEY) // Or get if from a file?

    licenseUrl = "http://www.opensource.org/licenses/MIT" // Or whatever license you use
    licenseName = "The MIT License" // Or whatever license you use
}
```

### Run!

Run `./gradlew publishModules` and it will publish all the possible modules in the right way, even if some are AAR and others JAR and depend between them :)

### Relevant notes

All properties ar self explanatory, except `localArtifacts`. Whats this?

The `localArtifacts` is used in case you have inter-module dependencies.

Lets say you have a project with module A and B. B uses A like `compile project(path: 'A')`. When B gets published, we need A published too, else the pom will have a reference to a local module (with "undefined" version). This is bad!

When we see a version has an undetermined version, we look in the `localArtifacts` for the artifact, if it exists, then that dependency was a local one! So we change its version to the release that was performed seconds ago.

Example of what happens (this is pseudocode, please dont think that this is exactly what really happens):

```
Modules: A and B
Dependencies: B uses A
Publishing version: 1.2.2
Running './gradlew publishModules'
:Prepare and assemble/test/etc A module (since its lower in the graph than B)
:publishes A 1.2.2
:Prepare and assemble/etc B module
:B has an undefined version!
:Is the version a local artifact? (Check in the localArtifacts array of the publish extension)
:It is! Is A. Changing version 'undefined' to '1.2.2'
:A version 1.2.2 was published just a minute ago, so we find it!
:publishes B 1.2.2
```

### Contributing

Please feel free to fork and do me a PR or file me issues!