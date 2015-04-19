[![Build Status](https://travis-ci.org/aaschmid/gradle-cpd-plugin.png?branch=master)](https://travis-ci.org/aaschmid/gradle-cpd-plugin)
[![Download](https://api.bintray.com/packages/aaschmid/gradle-plugins/gradle-cpd-plugin/images/download.svg)](https://bintray.com/aaschmid/gradle-plugins/gradle-cpd-plugin/_latestVersion)
[![Issues](https://img.shields.io/github/issues/aaschmid/gradle-cpd-plugin.svg)](https://github.com/aaschmid/gradle-cpd-plugin/issues)
[![Forks](https://img.shields.io/github/forks/aaschmid/gradle-cpd-plugin.svg)](https://github.com/aaschmid/gradle-cpd-plugin/network)
[![Stars](https://img.shields.io/github/stars/aaschmid/gradle-cpd-plugin.svg)](https://github.com/aaschmid/gradle-cpd-plugin/stargazers)


Gradle CPD plugin
=================

##### Table of Contents
* [What is it](#what-is-it)
* [Requirements](#requirements)
* [Usage](#usage)
* [Options](#options)
* [Contributing](#contributing)
* [Release notes](/../../releases)


What is it
----------

A [Gradle](http://gradle.org) plugin to find duplicate code using [PMD](http://pmd.sourceforge.net)s copy/paste detection (= [CPD](http://pmd.sourceforge.net/usage/cpd-usage.html)).


Requirements
------------

Currently this plugin requires [PMD]() greater or equal to version 5.2 such that ```toolVersion >= v5.2.0```.

Explaination: As [PMD]()s source code and artifacts were modularized into modules for every language with v5.2.0 (see
[Changelog - 5.2.0](http://pmd.sourceforge.net/pmd-5.2.0/overview/changelog.html)) this plugin uses the
'pmd-dist' dependency by default.  This dependency further includes 'pmd-core', 'pmd-java', 'pmd-cpp',
... transitively. This also forces proper working of ```toolVersion``` to [PMD]() v5.2.0 and higher. If
you want to use a version prior to v5.2.0, you can use the following snipped

```groovy
dependencies {
    cpd 'net.sourceforge.pmd:pmd:5.0.5'
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
        classpath 'de.aaschmid.gradle.plugins:gradle-cpd-plugin:0.4'
    }
}

cpd {
    language = 'cpp'
    toolVersion = '5.2.3' // defaults to '5.3.0'; just available for v5.2.0 and higher (see explanation above)
}

// optional - default report is xml and default sources are 'main' and 'test'
cpdCheck {
    reports {
        text.enabled = true
        xml.enabled = false
    }
    source = sourceSets.main.allJava // only java, groovy and scala classes in 'main' sourceSets
}
```

By default the copy-paste-detection looks at all source code of all subprojects which at least apply ```JavaBasePlugin```
(explicitly or implicitly trough e.g. Java or Groovy plugin).

*Note:* With v0.2, I have renamed the default task from ```cpd``` to ```cpdCheck``` that it does not have a name clash anymore.


Options
-------

This plugin supports the following options, either set for the plugin using ```cpd { }``` or for every task explicitly,
e.g. using ```cpdCheck { }```:

| Attribute         | Default              | Applies for ```language``` |
| ----------------- |:--------------------:|:--------------------------:|
| encoding          | System default       |                            |
| ignoreFailures    | ```false```          |                            |
| minimumTokenCount | ```50```             |                            |

For more information about options and their descriptions, see [here](http://pmd.sourceforge.net/usage/cpd-usage.html#Options).
To request more options, please file an issue [here](/../../issues).

Additionally, one can configure the following reports for every task analogous to
[Reporting](https://gradle.org/docs/current/dsl/org.gradle.api.reporting.Reporting.html) as for any other reporting plugin. See also
the example in [Usage](#usage) section above.

| Report | Default  | Further options and their defaults    |
| ------ |:--------:| ------------------------------------- |
| csv    | disabled | ```separator = ','```                 |
| text   | disabled | ```lineSeparator = '====================================================================='```, ```trimLeadingCommonSourceWhitespaces = false``` |
| xml    | enabled  | ```encoding = <<System default>>```   |


Contributing
------------

You are very welcome to contribute by providing a patch/pull request.
