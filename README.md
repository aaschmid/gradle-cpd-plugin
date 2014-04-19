[![Build Status](https://travis-ci.org/aaschmid/gradle-cpd-plugin.png?branch=master)](https://travis-ci.org/aaschmid/gradle-cpd-plugin)
[![Coverage Status](https://coveralls.io/repos/aaschmid/gradle-cpd-plugin/badge.png?branch=master)](https://coveralls.io/r/aaschmid/gradle-cpd-plugin)


Gradle CPD plugin
=================

##### Table of Contents
[What is it](#what-is-it)
[Requirements](#requirements)
[Usage](#usage)
[Contributing](#contributing)


What is it
----------

A [Gradle](http://gradle.org) plugin to find duplicate code using [PMD]s copy/paste detection (= [CPD](http://pmd.sourceforge.net/cpd-usage.html)).


Requirements
------------

Currently this plugin requires [PMD] greater or equal to version 5 such that ```toolVersion >= v5.0.0```. However you can try it with previous versions using

```groovy
dependencies {
    cpd 'pmd:pmd:4.2.5'
}
```


Usage
-----

*Information:* Downloading from [Maven Central](http://search.maven.org/) is coming soon.

# Clone this repository: ```git clone git@github.com:aaschmid/gradle-cpd-plugin.git```
# Change to clone repository: ```cd gradle-cpd-plugin/```
# Build project:
#* Windowns: ```gradlew.bat build```
#* Unix: ```./gradlew build```

This example assumes the former cloned ```gradle-cpd-plugin``` project to be in the same folder as the project to be checked for duplicates (otherwise change path to ```classpath```):

```groovy
apply plugin: 'cpd'

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath files('../gradle-cpd-plugin/build/libs/gradle-cpd-plugin-0.1.jar')
    }
}

cpd {
    toolVersion = '5.1.0'
}

tasks.cpd {
    reports {
        text.enabled = true
        xml.enabled = false
    }
    source = files('src/main/java')
}
```


Contributing
------------

You are very welcome to contribute by providing a patch/pull request.
