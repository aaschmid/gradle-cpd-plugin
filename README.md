[![Build Status](https://travis-ci.org/aaschmid/gradle-cpd-plugin.png?branch=master)](https://travis-ci.org/aaschmid/gradle-cpd-plugin)


Gradle CPD plugin
=================

##### Table of Contents
* [What is it](#what-is-it)
* [Requirements](#requirements)
* [Usage](#usage)
* [Release notes](#release-notes)
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

*Note:* I am just not happy with the naming of ```cpd``` extension (see toolVersion) and task, as it is the same currently and the task has to be referenced as ```tasks.cpd```. Suggestions welcome via issue ([here](https://github.com/aaschmid/gradle-cpd-plugin/issues/new)). Thanks in advance. ;-)

This example shows a project where only  ```main``` sources should be checked for duplicates:


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

// optional - default report is xml and default source is 'main' and 'test' 
tasks.cpd {
    reports {
        text.enabled = true
        xml.enabled = false
    }
    source = sourceSets.main.allJava // only java, groovy and scala classes in 'main' sourceSets
}
```

If you want to run one copy-paste-detection for all subprojects which have got sourceSets, you can configure the cpd task as follows:

```groovy
tasks.cpd {
    allprojects.findAll{ p -> p.hasProperty('sourceSets') }.each{ p ->
        p.sourceSets.all{ sourceSet -> source sourceSet.allJava }
    }
}
```


Release notes
-------------

### tbd. (tbd.)

* Directly call CPD instead of using Gradle's ```AntBuilder``` ([#1](/../../issues/1))
* Use ```sourceSets``` of the project which is plugin applied to by default ([#4](/../../issues/4))
* Added docu for multi-module build and CPD for the whole project ([#6](/../../issues/6))
* ...

### v0.1 (25-Apr-2014)

* initial release


Contributing
------------

You are very welcome to contribute by providing a patch/pull request.
