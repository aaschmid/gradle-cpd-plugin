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

A [Gradle](http://gradle.org) plugin to find duplicate code using [PMD](http://pmd.sourceforge.net)s copy/paste detection (= [CPD](http://pmd.sourceforge.net/cpd-usage.html)).


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

// optional - default is 5.2.3
cpd {
    // As PMD was split with v5.2.0 and CPD has moved to 'pmd-core', 'toolVersion' is just available for 5.2.0 and higher
    toolVersion = '5.2.1'
}

// optional - default report is xml and default source is 'main' and 'test' 
cpdCheck {
    reports {
        text.enabled = true
        xml.enabled = false
    }
    source = sourceSets.main.allJava // only java, groovy and scala classes in 'main' sourceSets
}
```

If you want to run one copy-paste-detection for all subprojects which have got sourceSets, you can configure the cpd task as follows:

```groovy
cpdCheck {
    allprojects.findAll{ p -> p.hasProperty('sourceSets') }.each{ p ->
        p.sourceSets.all{ sourceSet -> source sourceSet.allJava }
    }
}
```

*Note:* With v0.2, I have renamed the default task from ```cpd``` to ```cpdCheck``` that it does no long have a name clash.

Release notes
-------------

### tbd. (tbd.)

* ...

### [v0.3](http://search.maven.org/#artifactdetails|de.aaschmid.gradle.plugins|gradle-cpd-plugin|0.3|jar) (12-Feb-2015)

* fixed NPE if a task before 'check' is executed ([#8](/../../issues/8))


### [v0.2](http://search.maven.org/#artifactdetails|de.aaschmid.gradle.plugins|gradle-cpd-plugin|0.2|jar) (8-Feb-2015)

*NOTE*: Unfortunately, this version has introduced [#8](/../../issues/8), therefore please use [#v0.3] instead!

* Directly call CPD instead of using Gradle's ```AntBuilder``` ([#1](/../../issues/1))
* Use ```sourceSets``` of the project which is plugin applied to by default ([#4](/../../issues/4))
* Added docu for multi-module build and CPD for the whole project ([#6](/../../issues/6))
* Added warning if cpd task is not executed because only subprojects apply 'java' plugin ([#3](/../../issues/3))


### [v0.1](http://search.maven.org/#artifactdetails|de.aaschmid.gradle.plugins|gradle-cpd-plugin|0.1|jar) (25-Apr-2014)

* initial release


Contributing
------------

You are very welcome to contribute by providing a patch/pull request.
