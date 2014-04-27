[![Build Status](https://travis-ci.org/aaschmid/gradle-cpd-plugin.png?branch=master)](https://travis-ci.org/aaschmid/gradle-cpd-plugin)


Gradle CPD plugin
=================

##### Table of Contents
* [What is it](#what-is-it)
* [Requirements](#requirements)
* [Usage](#usage)
* [Contributing](#contributing)


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

This plugin is available from [Maven Central](http://search.maven.org/), see [here](http://search.maven.org/#search|ga|1|gradle-cpd-plugin).


This example shows a project which ```src/main/java``` folder should be checked for duplicates:


```groovy
apply plugin: 'cpd'

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'de.aaschmid.gradle.plugins:gradle-cpd-plugin:0.1'
    }
}

// optional - default is 5.1.0
cpd {
    toolVersion = '5.0.5'
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
