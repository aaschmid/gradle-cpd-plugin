package de.aaschmid.gradle.plugins.cpd.test

import spock.lang.Issue

import java.nio.file.Files

import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.*
import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.Lang.*
import static org.gradle.testkit.runner.TaskOutcome.*

class CpdAcceptanceTest extends IntegrationBaseSpec {

    def "Cpd will be skipped if no source is set"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                include '**/*.java'
                exclude '**/*1.java'
                exclude '**/*z.java'
                //source
            }
        """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == NO_SOURCE
        result.output.contains("BUILD SUCCESSFUL")
        !result.output.contains('WARNING: Due to the absence of \'LifecycleBasePlugin\' on root project')
    }

    def "Cpd fails if no report is enabled"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                reports{
                    csv.enabled = false
                    text.enabled = false
                    xml.enabled = false
                }
                source = '.'
            }
        """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == FAILED
        result.output.contains("BUILD FAILED")
        result.output.contains("Task 'cpdCheck' requires at least one enabled report.")

        !file('build/reports/cpdCheck.csv').exists()
    }

    def "Cpd will produce empty 'cpdCheck.xml' on non-duplicate 'java' source"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpd{
                encoding = 'ISO-8859-1'
                minimumTokenCount = 10
            }
            cpdCheck.source = ${testPath(JAVA, 'de/aaschmid/foo')}
        """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = testProjectDir.getRoot().toPath().resolve('build/reports/cpd/cpdCheck.xml').toFile()
        report.exists()
        report.text =~ /encoding="ISO-8859-1"/
        report.text =~ /<pmd-cpd\/>/
    }

    @Issue("https://github.com/aaschmid/gradle-cpd-plugin/issues/38")
    def "Cpd should not produce 'cpdCheck.txt' on duplicate 'java' comments in source"() {
        given:
        buildFileWithPluginAndRepos([ 'java' ]) << """
            cpdCheck{
                reports{
                    xml.enabled = false
                    text.enabled = true
                }
                source = ${testPath(JAVA, 'de/aaschmid/test', 'de/aaschmid/duplicate')}
            }
        """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.text')
        report.exists()
        report.text.empty
    }

    @Issue("https://github.com/aaschmid/gradle-cpd-plugin/issues/38")
    def "Cpd should not produce 'cpdCheck.txt' on duplicate 'kotlin' comments in source"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                language = 'kotlin'
                minimumTokenCount = 10
                reports{
                    xml.enabled = false
                    text.enabled = true
                }
                source = ${testPath(KOTLIN, 'de/aaschmid/test')}
            }
        """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.text')
        report.exists()
        report.text.empty
    }

    def "Cpd should fail and produce 'cpdCheck.csv' with one warning on duplicate 'java' source"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                minimumTokenCount = 15
                reports{
                    csv.enabled = true
                    xml.enabled = false
                }
                source = ${testPath(JAVA, 'de/aaschmid/clazz')}
            }
        """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == FAILED
        result.output.contains("BUILD FAILED")
        result.output =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /4,15,2,[79],.*Clazz[12]\.java,[79],.*Clazz[12]\.java/
    }

    def "Cpd should not fail if 'ignoreFailures' and produce 'cpdCheck.csv' with one warning on duplicate 'java' source"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreFailures = true
                minimumTokenCount = 15
                reports{
                    csv.enabled = true
                    xml.enabled = false
                }
                source files(${testPath(JAVA, 'de/aaschmid/clazz')})
            }
        """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /4,15,2,[79],.*Clazz[12]\.java,[79],.*Clazz[12]\.java/
    }

    def "Cpd should produce result with automantic detection of subProjects sources if Cpd applied to parent project and 'java' plugin to sub project"() {
        given:
        withSubProjects('sub')

        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreFailures = true
                minimumTokenCount = 2
            }

            project(':sub') {
                apply plugin: 'java'
            }
        """.stripIndent()

        file("sub/src/main/java/de/aaschmid/foo").mkdirs()
        Files.copy(testFile(JAVA, 'de/aaschmid/foo/Bar.java').toPath(), file("sub/src/main/java/de/aaschmid/foo/Bar.java").toPath())
        Files.copy(testFile(JAVA, 'de/aaschmid/foo/Baz.java').toPath(), file("sub/src/main/java/de/aaschmid/foo/Baz.java").toPath())

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        report.text =~ /Bar.java/
        report.text =~ /Baz.java/
    }

    def "Cpd should produce result if applied to only parent project even if only sub project has 'groovy' plugin and sources"() {
        given:
        withSubProjects('subProject')

        buildFileWithPluginAndRepos() << """
            project(':subProject') {
                apply plugin: 'groovy'

                sourceSets{
                    main{
                        java.srcDirs = ${testPath(JAVA, 'de/aaschmid/foo')}
                        groovy.srcDir files(${testPath(JAVA, 'de/aaschmid/clazz')})
                    }
                }
            }

            // configure Cpd at last because otherwise test fails, maybe because of custom source set
            cpdCheck{
                ignoreFailures = true
                minimumTokenCount = 2
            }
        """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")


        def report = file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        report.text =~ /Bar.java/
        report.text =~ /Baz.java/
        report.text =~ /Clazz.java/
        report.text =~ /Clazz1.java/
        report.text =~ /Clazz2.java/
    }

    def "Cpd should create and fill all enabled reports"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck {
                language = 'kotlin'
                minimumTokenCount = 5
                reports {
                    csv.enabled = false
                    text.enabled = true
                    xml.enabled = true
                }
                source = ${testPath(KOTLIN, '.')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == FAILED
        result.output.contains("BUILD FAILED")

        def csv = file('build/reports/cpd/cpdCheck.csv')
        def txt = file('build/reports/cpd/cpdCheck.text')
        def xml = file('build/reports/cpd/cpdCheck.xml')

        !csv.exists()

        txt.exists()
        txt.text.contains("Found a 4 line (9 tokens) duplication in the following files:")
        txt.text.contains("Found a 2 line (8 tokens) duplication in the following files:")

        xml.exists()
        xml.text.contains('<duplication lines="4" tokens="9">')
        xml.text.contains('<duplication lines="2" tokens="8">')
    }

    @Issue("https://github.com/aaschmid/gradle-cpd-plugin/issues/39")
    def "two Cpd tasks for different sources should produce proper results"() {
        given:
        buildFileWithPluginAndRepos([ 'java' ]) << """
            cpd {
                minimumTokenCount = 5
            }

            cpdCheck{
                minimumTokenCount = 2
                reports{
                    ignoreFailures = true
                    csv{
                        destination = file('java-cpd.csv')
                        enabled = true
                    }
                    xml.enabled = false
                }
                source = ${testPath(JAVA, 'de/aaschmid/foo')}
            }

            task cpdCheckKotlin(type: de.aaschmid.gradle.plugins.cpd.Cpd){
                reports{
                    language = "kotlin"
                    xml{
                        destination = file('kotlin-cpd.xml')
                    }
                }
                source = ${testPath(KOTLIN, 'de/aaschmid/test')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck", "cpdCheckKotlin")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.task(':cpdCheckKotlin').outcome == FAILED
        result.output.contains("BUILD FAILED")

        def javaReport = file('java-cpd.csv')
        javaReport.exists()
        javaReport.text =~ /Bar.java/
        javaReport.text =~ /Baz.java/

        def kotlinReport = file('kotlin-cpd.xml')
        kotlinReport.exists()
        kotlinReport.text.contains('<duplication lines="4" tokens="9">')
        kotlinReport.text.contains('<duplication lines="2" tokens="8">')
    }

    def "Cpd should fail if not ignoreAnnotations on duplicate annotations"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreAnnotations = false
                minimumTokenCount = 40
                reports{
                    csv.enabled = true
                    xml.enabled = false
                }
                source = ${testPath(JAVA, 'de/aaschmid/annotation')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == FAILED
        result.output.contains("BUILD FAILED")
        result.output =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        // locally Person.java comes before Employee, on travis-ci is Employee first => make it irrelevant
        report.text =~ /8,53,2,6,.*(Person|Employee)\.java,6,.*(Person|Employee)\.java/
        report.text =~ /14,45,2,13,.*(Person|Employee)\.java,13,.*(Person|Employee)\.java/
    }

    def "Cpd should not fail if ignoreAnnotations on duplicate annotations"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreAnnotations = true
                minimumTokenCount = 40
                reports {
                    vs.enabled = true
                    xml.enabled = false

                }
                source = ${testPath(JAVA, 'de/aaschmid/annotation')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.vs')
        report.exists()
        report.text.isEmpty()
    }

    def "Cpd should fail if ignoreIdentifiers on different identifiers"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreIdentifiers = true
                minimumTokenCount = 15
                reports{
                    csv.enabled = true
                    xml.enabled = false
                }
                source = ${testPath(JAVA, 'de/aaschmid/identifier')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == FAILED
        result.output.contains("BUILD FAILED")
        result.output =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /6,19,2,3,.*Identifier[12]\.java,3,.*Identifier[12]\.java/
    }

    def "Cpd should not fail if not ignoreIdentifiers on different annotations"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreIdentifiers = false
                minimumTokenCount = 15
                source = ${testPath(JAVA, 'de/aaschmid/identifier')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        report.text =~ /<pmd-cpd\/>/
    }

    def "Cpd should fail if ignoreLiterals on different literals"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreLiterals = true
                minimumTokenCount = 20
                reports{
                    vs.enabled = true
                }
                source = ${testPath(JAVA, 'de/aaschmid/literal')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == FAILED
        result.output.contains("BUILD FAILED")
        result.output =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.vs/

        def vsReport = file('build/reports/cpd/cpdCheck.vs')
        vsReport.exists()
        vsReport.text =~ /Literal[12].java\(5\): Between lines 5 and 14/

        def xmlReport = file('build/reports/cpd/cpdCheck.xml')
        xmlReport.exists()
        xmlReport.text =~ /<duplication lines="9" tokens="27">\s+<file line="5"\s+path=".*Literal[12].java"\/>/
    }

    def "Cpd should not fail if not ignoreLiterals on different literals"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreLiterals = false
                minimumTokenCount = 20
                source = ${testPath(JAVA, 'de/aaschmid/literal')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        report.text =~ /<pmd-cpd\/>/
    }

    def "Cpd should fail if not skipDuplicateFiles on duplicate files"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                minimumTokenCount = 5
                reports{
                    csv.enabled = true
                    xml.enabled = false
                }
                skipDuplicateFiles = false
                source = ${testPath(JAVA, 'de/aaschmid/duplicate', 'de/aaschmid/test')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == FAILED
        result.output.contains("BUILD FAILED")
        result.output =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /6,15,2,20,.*(duplicate|test)\/Test\.java,20,.*(duplicate|test)\/Test\.java/
    }

    def "Cpd should not fail if skipDuplicateFiles on duplicate files"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                minimumTokenCount = 5
                skipDuplicateFiles = true
                source = ${testPath(JAVA, 'de/aaschmid/duplicate', 'de/aaschmid/test')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        report.text =~ /<pmd-cpd\/>/
    }


    def "Cpd should fail if not skipLexicalErrors on files containing lexical errors"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                skipLexicalErrors = false
                source = ${testPath(JAVA, 'de/aaschmid/lexical')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == FAILED
        result.output.contains("BUILD FAILED")
        result.output =~ /Lexical error in file .*Error.java at/

        def report = file('build/reports/cpd/cpdCheck.csv')
        !report.exists()
    }

    def "Cpd should not fail if skipLexicalErrors on files containing lexical errors"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                skipLexicalErrors = true
                source = ${testPath(JAVA, 'de/aaschmid/lexical')}
            }
            """.stripIndent()

        when:
        def result = run("cpdCheck")

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        report.text =~ /<pmd-cpd\/>/
    }

    def "Cpd should be up-to-date on second run with same input"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreFailures = true
                minimumTokenCount = 5
                reports{
                    vs.enabled = true
                    xml.enabled = false
                }
                exclude '**/lexical/**'
                source = ${testPath(JAVA, '.')}
            }
            """.stripIndent()

        when:
        def firstResult = run("cpdCheck")
        then:
        firstResult.task(':cpdCheck').outcome == SUCCESS
        firstResult.output.contains("BUILD SUCCESSFUL")

        when:
        def secondResult = run("cpdCheck")
        then:
        secondResult.task(':cpdCheck').outcome == UP_TO_DATE
        secondResult.output.contains("BUILD SUCCESSFUL")
        !(secondResult.output =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.vs/)
    }

    def "Cpd should be loaded from cache on second run with same input on clean build"() {
        given:
        settingsFile << """
            buildCache {
                local(DirectoryBuildCache) {
                    directory = "\${rootDir}/build-cache"
                }
            }
            """.stripIndent()

        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreFailures = true
                minimumTokenCount = 5
                reports{
                    vs.enabled = true
                    xml.enabled = false
                }
                exclude '**/lexical/**'
                source = ${testPath(JAVA, '.')}
            }
            """.stripIndent()

        when:
        def firstResult = run('--build-cache', "cpdCheck")
        then:
        firstResult.task(':cpdCheck').outcome == SUCCESS
        firstResult.output.contains("BUILD SUCCESSFUL")

        and:
        file('build').deleteDir()

        when:
        def secondResult = run('--build-cache', "cpdCheck")
        then:
        secondResult.task(':cpdCheck').outcome == FROM_CACHE
        secondResult.output.contains("BUILD SUCCESSFUL")
        !(secondResult.output =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.vs/)
    }

    @Issue("https://github.com/aaschmid/gradle-cpd-plugin/issues/54")
    def "Cpd task can be loaded from the configuration cache"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
//                ignoreFailures = true
//                minimumTokenCount = 15
                reports{
                    csv.enabled = true
                    xml.enabled = false
                }
                source files(${testPath(JAVA, 'de/aaschmid/clazz')})
            }
            """.stripIndent()

        when:
        def result = run("--configuration-cache", "--configuration-cache-problems=warn", "cpdCheck")
        println result.output

        then:
        result.task(':cpdCheck').outcome == SUCCESS
        result.output.contains("BUILD SUCCESSFUL")

        def report = file('build/reports/cpd/cpdCheck.csv')
        report.exists()
//        report.text =~ /4,15,2,[79],.*Clazz[12]\.java,[79],.*Clazz[12]\.java/

        // remove report in order to force re-execution of `cpdCheck`
        report.delete()

        when:
        def result2 = run("--configuration-cache", "--configuration-cache-problems=warn", "cpdCheck")
        println result2.output

        then:
        result2.task(':cpdCheck').outcome == SUCCESS
        result2.output.contains('Reusing configuration cache.')
        result2.output.contains("BUILD SUCCESSFUL")

        def report2 = file('build/reports/cpd/cpdCheck.csv')
        report2.exists()
//        report2.text =~ /4,15,2,[79],.*Clazz[12]\.java,[79],.*Clazz[12]\.java/
    }
}
