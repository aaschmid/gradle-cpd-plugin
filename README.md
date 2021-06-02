[![TravisCI](https://travis-ci.org/aaschmid/gradle-cpd-plugin.svg?branch=master)](https://travis-ci.org/aaschmid/gradle-cpd-plugin)
[![CircleCI](https://circleci.com/gh/aaschmid/gradle-cpd-plugin.svg?style=svg)](https://circleci.com/gh/aaschmid/gradle-cpd-plugin)
[![Coverage Status](https://coveralls.io/repos/github/aaschmid/gradle-cpd-plugin/badge.svg?branch=master)](https://coveralls.io/github/aaschmid/gradle-cpd-plugin?branch=master)
[![codecov](https://codecov.io/gh/aaschmid/gradle-cpd-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/aaschmid/gradle-cpd-plugin)
[![codebeat badge](https://codebeat.co/badges/ea643af5-89e9-4b6c-8d46-11199fcac3b5)](https://codebeat.co/projects/github-com-aaschmid-gradle-cpd-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.aaschmid/gradle-cpd-plugin/badge.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22de.aaschmid%22%20AND%20a%3A%22gradle-cpd-plugin%22)
[![Bintray](https://api.bintray.com/packages/aaschmid/gradle-plugins/gradle-cpd-plugin/images/download.svg)](https://bintray.com/aaschmid/gradle-plugins/gradle-cpd-plugin/_latestVersion)
[![license](https://img.shields.io/github/license/aaschmid/gradle-cpd-plugin.svg)](https://github.com/aaschmid/gradle-cpd-plugin)
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

A [Gradle](http://gradle.org) plugin to find duplicate code using [PMD][]s copy/paste detection (= [CPD][]). See also
https://plugins.gradle.org/plugin/de.aaschmid.cpd.


Requirements
------------

Currently this plugin requires [PMD](https://pmd.github.io/) greater or equal to version 6.1.0 such that ```toolVersion >= '6.1.0'```.

Explanation: v2.0 removes deprecated rendering API (= `net.sourceforge.pmd.cpd.Renderer`) and replace it with new
`net.sourceforge.pmd.cpd.renderer.CPDRenderer` which was introduced with v6.1.0, see
[PMD release notes](https://pmd.github.io/2018/02/25/PMD-6.1.0/#api-changes).

### Supported versions

| G. CPD plugin  | Gradle       | PMD         | Java¹  |
|:-------------- |:------------ |:----------- |:------ |
| [v0.1][]       | 1.10 - 4.x   | 5.0.0 - 5.x | 6 - 8  |
| [v0.2][]       | 2.0 - 4.x    | 5.0.0 - 5.x | 6 - 8  |
| [v0.4][]       | 2.3 - 4.x    | 5.2.0 - 5.x | 6 - 8  |
| [v1.0][]       | 2.14 - 5.0   | 5.2.0 - 5.x | 6 - 8  |
| [v1.1][]       | 2.14 - 5.0   | 5.2.2 - 6.x | 6 - 9  |
| [v1.2][]       | >= 3.5.1     | >= 5.2.2    | >= 8   |
| [v1.3][]       | >= 4.10.3    | >= 5.2.2    | >= 8   |
| [v2.0][]       | 4.10.3 - 5.5 | >= 6.1.0    | >= 8   |
| [v3.0][]       | 5.6 - 5.x    | >= 6.10.0   | >= 8   |
| [v3.1][]       | >= 5.6       | >= 6.10.0   | >= 8   |
| [v3.2][]       | >= 6.6       | >= 6.10.0   | >= 8   |
| [v3.3][]       | >= 6.6       | >= 6.10.0   | >= 8   |

¹: Java version may additionally depend on [PMD][]s version which is might not be properly reflected here.

Usage
-----

This plugin is available using either the new [Gradle plugins DSL](https://gradle.org/docs/current/userguide/plugins.html#sec:plugins_block)


```groovy
plugins {
    id 'de.aaschmid.cpd' version '2.0'
}
```

or the old fashion [buildscript block](https://gradle.org/docs/current/userguide/plugins.html#sec:applying_plugins_buildscript)
from [Maven Central](http://search.maven.org/#search|ga|1|gradle-cpd-plugin) or
[jCenter](https://bintray.com/aaschmid/gradle-plugins/gradle-cpd-plugin/view).
```groovy
buildscript {
    repositories {
        // choose your prefered one
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath 'de.aaschmid:gradle-cpd-plugin:2.0'
    }
}
apply plugin: 'cpd'
```

**Attention:** The plugins groupId was changed from ```de.aaschmid.gradle.plugins``` to ```de.aaschmid``` in [v1.0][].

By default the copy-paste-detection looks at all source code of all projects which at least apply ```LifecycleBasePlugin```.
If you use a different programming language and want to get it configured out of the box, please open an issue :-)

### Single module project

If you have a single module project you just need to make sure that the ```LifecycleBasePlugin``` is also applied to it
(explicitly or implicitly through e.g. Java or Groovy plugin). Otherwise you can simply add a dependency to a task you like by

```groovy
analyze.dependsOn(cpdCheck)
```

### Multi module project

If the root project of your multi-module project applies the ```LifecycleBasePlugin```, you are done. But most likely this
is not how your project looks like. If so, you need to manually add a task graph dependency manually to either one or all
of your subprojects such that the ```cpdCheck``` task is executed:

```groovy
// one single subproject where 'LifecycleBasePlugin' is available
subprojectOne.check.dependsOn(':cpdCheck')

// all subprojects where 'check' task is available (which comes with 'LifecycleBasePlugin')
subprojects {
    plugins.withType(LifecycleBasePlugin) { // <- just if 'LifecycleBasePlugin' plugin is not applied to all subprojects
        check.dependsOn(rootProject.cpdCheck)
    }
}
```

### Custom sourceSets

If your are adding custom sourceSets (even in subProjects), it may occur that you either need to configure `cpdCheck`
afterwards or even manually configure `source`. Unfortunately, I have had problems with this case in the
[CpdAcceptanceTest](https://github.com/aaschmid/gradle-cpd-plugin/blob/master/src/integTest/groovy/de/aaschmid/gradle/plugins/cpd/test/CpdAcceptanceTest.groovy)
tests using Gradle [TestKit](https://docs.gradle.org/current/userguide/test_kit.html).


### Examples

This example shows a project where only ```main``` sources should be checked for duplicates:

```groovy
// optional - settings for every CPD task
cpd {
    language = 'cpp'
    toolVersion = '6.1.0' // defaults to '6.13.0'; just available for v6.1.0 and higher (see explanation above)
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

*Note:* With v0.2, I have renamed the default task from ```cpd``` to ```cpdCheck``` that it does not have a name clash anymore.

### Property `source`

Depending on your needs, java sourcesets of all projects will be automatically added to `source` and therefore checked for duplicates (see [Code](https://github.com/aaschmid/gradle-cpd-plugin/blob/%2350.text.fixture/src/main/java/de/aaschmid/gradle/plugins/cpd/CpdPlugin.java#L79-L88)). You have a lot of options for overriding this behaviour but note that the lazy evaluation - especially later configured subprojects - could add further `sourceSets` again unless you use `[evaluationDependsOnChildren()](https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html#evaluationDependsOnChildren--)` for your `rootProject`:

```groovy
source = subprojects*.sourceSets*.main*.java*.srcDirs
```
or
```groovy
source = subprojects*.sourceSets*.main*.java*.srcDirs + subprojects*.sourceSets*.test*.java*.srcDirs
```
or
```groovy
cpdCheck {
    source = []
    allprojects.forEach { project ->
        project.plugins.withType(JavaPlugin) { plugin ->
            project.sourceSets.main.java.forEach { s -> rootProject.cpdCheck.source(s) }
        }
    }
}
```
or same using kotlin DSL (Note: That works only if all `subprojects` have `sourceSets` `main` and `test` e.g. by appling the `java` plugins. Otherwise you can just filter for `JavaBasePlugin` before)
```kotlin
setSource(files(
    // only check java source code
    subprojects.flatMap { it.the<SourceSetContainer>()["main"].java.srcDirs },
    subprojects.flatMap { it.the<SourceSetContainer>()["test"].java.srcDirs }
))
```
or if you need a new cpd task for kotlin and not all `subprojects` apply a `java` plugin (see also [here](https://github.com/aaschmid/gradle-cpd-plugin/issues/39#issuecomment-488730600))
```kotlin
tasks.register<Cpd>("cpdKotlin") {
    language = "kotlin"
    exclude { it.file.extension.contains("java") }

    allprojects.forEach { project ->
        project.plugins.withType<JavaPlugin> {
            project.convention.getPlugin<JavaPluginConvention>().sourceSets.configureEach {
                allJava.srcDirTrees.forEach { this@register.source(it) }
            }
        }
    }
}
```

### Kotlin support

[CPD][] supports [Kotlin](https://kotlinlang.org/) since
[v6.10.0](https://search.maven.org/search?q=g:net.sourceforge.pmd%20AND%20a:pmd-kotlin&core=gav). For previous versions
you have to ignore files manually if you mixed it up with your Java files:
```groovy
tasks.withType(Cpd) {
    exclude "*.kt"
}
```

Options
-------

This plugin supports the following options, either set for the plugin using ```cpd { }``` or for every task explicitly,
e.g. using ```cpdCheck { }```:

| Attribute          | Default              | Applies for ```language``` | since    |
| ------------------ |:--------------------:|:--------------------------:|:--------:|
| encoding           | System default       |                            | [v0.1][] |
| ignoreAnnotations  | ```false```          | ```'java'```               | [v0.4][] |
| ignoreFailures     | ```false```          |                            | [v0.1][] |
| ignoreIdentifiers  | ```false```          | ```'java'```               | [v0.4][] |
| ignoreLiterals     | ```false```          | ```'java'```               | [v0.4][] |
| language           | ```'java'```         |                            | [v0.4][] |
| minimumTokenCount  | ```50```             |                            | [v0.1][] |
| skipDuplicateFiles | ```false```          |                            | [v0.5][] |
| skipLexicalErrors  | ```false```          |                            | [v0.5][] |
| skipBlocks         | ```true```           | ```'cpp'```                | [v0.4][] |
| skipBlocksPattern  | ```'#if 0\|#endif'``` | ```'cpp'```               | [v0.4][] |

If a specified `language` cannot be found, a fallback mechanism uses `net.sourceforge.pmd.cpd.AnyLanguage` instead. This
fallback language does not run ANTLR and therefore also checks duplicates in comments.

For more information about options and their descriptions, see [here](https://pmd.github.io/latest/pmd_userdocs_cpd.html#attribute-reference),
and for the available programming languages have a look on [CPD documentation](https://pmd.github.io/latest/pmd_userdocs_cpd.html#supported-languages).
To request more options, please file an issue [here](/../../issues).


Additionally, one can configure the following reports for every task analogous to
[Reporting](https://gradle.org/docs/current/dsl/org.gradle.api.reporting.Reporting.html) as for any other reporting plugin. See also
the example in [Usage](#usage) section above.

| Report | Default  | since    | Further options and their defaults                        |
| ------ |:--------:| -------- | --------------------------------------------------------- |
| csv    | disabled | [v0.1][] | ```separator = ','```, ```includeLineCount = true```**²** |
| text   | disabled | [v0.1][] | ```lineSeparator = '====================================================================='```, ```trimLeadingCommonSourceWhitespaces = false``` |
| vs     | disabled | [v3.1][] | ```encoding = <<System default>>```                       |
| xml    | enabled  | [v0.1][] | ```encoding = <<System default>>```                       |

²: Since [v3.1][] but note that property `includeLineCount` is originally named `lineCountPerFile` and meaning is inverted which means that
`false` shows line count and `true` hides it, see
[here](https://github.com/pmd/pmd/blob/master/pmd-core/src/main/java/net/sourceforge/pmd/cpd/CSVRenderer.java#L63).


Contributing
------------

You are very welcome to contribute by providing a patch/pull request.

Please note that running the test cases my take quite long because the acceptance test cases (see
```de.aaschmid.gradle.plugins.cpd.test.CpdAcceptanceTest``` will download [CPD][] and its dependencies for every version. I recommend to
get these dependencies in your ```localMaven()``` repository as the test cases look there for it first.

[PMD]: https://pmd.github.io/
[CPD]: https://pmd.github.io/latest/pmd_userdocs_cpd.html

[v0.1]: /../../releases/tag/v0.1
[v0.2]: /../../releases/tag/v0.2
[v0.4]: /../../releases/tag/v0.4
[v0.5]: /../../releases/tag/v0.5
[v1.0]: /../../releases/tag/v1.0
[v1.1]: /../../releases/tag/v1.1
[v1.2]: /../../releases/tag/v1.2
[v1.3]: /../../releases/tag/v1.3
[v2.0]: /../../releases/tag/v2.0
[v3.0]: /../../releases/tag/v3.0
[v3.1]: /../../releases/tag/v3.1
