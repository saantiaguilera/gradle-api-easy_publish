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
        
        // We also need the bintray and maven plugins!
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.7.3")
        classpath("com.github.dcendents:android-maven-gradle-plugin:1.5")
    }
}
```

This will create a single root task `publishModules` and for each module a task `publishModule`. Please read the _Run!_ part of the readme for more information

### Configure

This plugin detects automatically a publishable module based on:

* It has `apply plugin: 'java|groovy|something_that_applies_JavaPlugin_inside'` -> JAR module
* It has `apply plugin: 'com.android.library` -> AAR module
* It has something else -> Non publishable module

You have global configurations and module specific ones. You can define both, but always a module specific will take preference over the global ones

#### Global Configurations

You can define a global configuration in the root `build.gradle` like:
```gradle
publishGlobalConfigurations {
    groupId = 'com.my.library'                // Group
    versionName = '1.0.0'                     // Version

    bintrayRepository = 'maven'               // defaults to "maven"if not found

    artifactsMappings = [                     // Mappings of moduleName -> artifactName when publishing them
        'module1' : 'artifact1',              // In this case the module name is 'module1', but will be released as:
        'module2' : 'artifact2'               // "com.my.library:artifact1:1.0.0"
    ]

    url = "http://github.com/user/repo"       // github url

    bintrayApiKey = System.getenv('apikey')   // Api key of bintray
    bintrayUser = System.getenv('user')       // User of bintray

    licenseUrl = 'sourceforge_url'            // License url to find it
    licenseName = 'sourceforge_name'          // License full name

    publishOrder = [ 'module1', 'module2' ]   // The order they should be published if used global task
}
```

Just with this you could publish all of them, using the mappings you could map the modules names to the artifacts they will have.

By default, if no mappings specified, they will default to the module name

_Please note that the mappings are also used if a local dependency is found in another module and has to be resolved with a different artifact name._


#### Local configuration

For every module in their `build.gradle` you can provide a `publishConfigurations`. The extension should look like this:

```gradle
publishConfigurations {
    groupId = 'com.my.library'
    artifactId = 'core'
    versionName = '1.0.0'

    bintrayRepository = 'maven'

    url = "https://github.com/saantiaguilera/android-api-SecureKeys"

    bintrayUser = System.getenv('BINTRAY_USER')
    bintrayApiKey = System.getenv('BINTRAY_APIKEY')

    licenseUrl = "http://www.opensource.org/licenses/MIT"
    licenseName = "The MIT License"
}
```

If a module has declared X values, they will be used instead of the globals! You can play with both of them, using some in the global configurations and others more specific in each module.

### Run!

Run `./gradlew my_module:publishModule` and it will publish it :)

If in the global scope the `publishOrder` was specified, you can run `./gradlew publishModules` and all the modules will be published, even if some are AAR and others JAR and depend between them.

The `publishOrder` should be formed from 'depends from none' to 'depends from all'. Example

```
:Modules A, B, C, D
:A depends from B, C, D
:B depends from D
:C depends from B
:D depends from no local module
:publishOrder = [ 'D', 'B', 'C', 'A' ]
```

### Notes

- Even this plugin publishes with itself! Dog-fooding at its finest
- If a local dependency is found in another module (`compile project(':other_module')'`) it will be resolved as `groupId:thatModuleName|artifactMappedToTheModuleNameInGlobalConfigs:version`
- By default javadocs arent included in the bintray package. If you wish to also deploy javadoc, please create a task `javadocJar` in your project and we will detect it and automatically add them :)

### Contributing

Please feel free to fork and do me a PR or file me issues!
