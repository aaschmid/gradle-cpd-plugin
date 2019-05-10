package de.aaschmid.gradle.plugins.cpd

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import net.sourceforge.pmd.cpd.Tokenizer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration.State
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testfixtures.ProjectBuilder

class CpdPluginTest extends BaseSpec {

    def "applying 'CpdPlugin' applies required reporting-base plugin"() {
        expect:
        project.plugins.hasPlugin(ReportingBasePlugin)
    }

    def "applying 'CpdPlugin' creates and configures extension 'cpd'"() {
        given:
        CpdExtension ext = project.extensions.findByName('cpd')

        expect:
        ext.encoding == System.getProperty('file.encoding')
        !ext.ignoreAnnotations
        !ext.ignoreIdentifiers
        !ext.ignoreFailures
        !ext.ignoreLiterals
        ext.language == 'java'
        ext.minimumTokenCount == 50
        !ext.skipDuplicateFiles
        !ext.skipLexicalErrors
        ext.skipBlocks
        ext.skipBlocksPattern == Tokenizer.DEFAULT_SKIP_BLOCKS_PATTERN
        ext.toolVersion == '6.13.0'
    }

    def "applying 'CpdPlugin' creates and configures configuration 'cpd'"() {
        given:
        def config = project.configurations.findByName('cpd')

        expect:
        config != null
        config.dependencies.empty
        config.description == 'The CPD libraries to be used for this project.'
        config.extendsFrom.empty
        config.state == State.UNRESOLVED
        config.transitive
        !config.visible
    }

    def "applying 'CpdPlugin' creates and configures task 'cpdCheck' with correct default values"() {
        given:
        Cpd task = project.tasks.getByName('cpdCheck')

        expect:
        task instanceof Cpd
        task.description == 'Run CPD analysis for all sources'
        task.group == null

        task.encoding == System.getProperty('file.encoding')
        !task.ignoreAnnotations
        !task.ignoreFailures
        !task.ignoreIdentifiers
        !task.ignoreLiterals
        task.language == 'java'
        task.minimumTokenCount == 50

        task.pmdClasspath == project.configurations.findByName('cpd')

        task.reports.csv.destination == project.file('build/reports/cpd/cpdCheck.csv')
        !task.reports.csv.enabled
        task.reports.text.destination == project.file('build/reports/cpd/cpdCheck.text')
        !task.reports.text.enabled
        task.reports.xml.destination == project.file('build/reports/cpd/cpdCheck.xml')
        task.reports.xml.enabled

        !task.skipDuplicateFiles
        !task.skipLexicalErrors
        task.skipBlocks
        task.skipBlocksPattern == Tokenizer.DEFAULT_SKIP_BLOCKS_PATTERN

        task.source.empty
    }

    def "applying 'CpdPlugin' configures additional tasks of type 'Cpd' using defaults"() {
        given:
        def task = project.tasks.create('cpdCustom', Cpd)

        expect:
        task instanceof Cpd
        task.description == null
        task.group == null

        task.encoding == System.getProperty('file.encoding')
        !task.ignoreAnnotations
        !task.ignoreFailures
        !task.ignoreIdentifiers
        !task.ignoreLiterals
        task.language == 'java'
        task.minimumTokenCount == 50

        task.pmdClasspath == project.configurations.cpd

        task.reports.csv.destination == project.file('build/reports/cpd/cpdCustom.csv')
        !task.reports.csv.enabled
        task.reports.text.destination == project.file('build/reports/cpd/cpdCustom.text')
        !task.reports.text.enabled
        task.reports.xml.destination == project.file('build/reports/cpd/cpdCustom.xml')
        task.reports.xml.enabled

        !task.skipDuplicateFiles
        !task.skipLexicalErrors
        task.skipBlocks
        task.skipBlocksPattern == Tokenizer.DEFAULT_SKIP_BLOCKS_PATTERN

        task.source.empty
    }

    def "applying 'CpdPlugin' and 'LifecycleBasePlugin' adds cpd tasks to check lifecycle task"() {
        given:
        project.plugins.apply(LifecycleBasePlugin)

        def checkTask = project.tasks.getByName('check')
        def cpdTask = project.tasks.getByName('cpdCheck')

        expect:
        checkTask.taskDependencies.getDependencies(checkTask).find{ Task task ->
            task == cpdTask
        }
    }

    def "applying 'CpdPlugin' and 'JavaPlugin' sets source with 'main' and 'test' sourceSets of Java project"() {
        given:
        project.plugins.apply(JavaPlugin)
        project.sourceSets{
            main{
                java.srcDir testFile('de/aaschmid/clazz')
                resources.srcDir testFile('de/aaschmid/foo')
            }
            test.java.srcDir testFile('de/aaschmid/test')
        }

        def cpdTask = project.tasks.getByName('cpdCheck')

        expect:
        cpdTask.source.files == [ *testFilesRecurseIn('de/aaschmid/clazz'), *testFilesRecurseIn('de/aaschmid/test') ] as Set
    }

    def "applying 'CpdPlugin' and 'GroovyPlugin' sets source with 'main' and 'test' sourceSets of Groovy project"() {
        given:
        project.plugins.apply(GroovyPlugin)
        project.sourceSets{
            main{
                groovy.srcDir testFile('de/aaschmid/clazz')
                resources.srcDir testFile('de/aaschmid/foo')
            }
            test.groovy.srcDir testFile('de/aaschmid/test')
        }

        def cpdTask = project.tasks.getByName('cpdCheck')

        expect:
        cpdTask.source.files == [ *testFilesRecurseIn('de/aaschmid/clazz'), *testFilesRecurseIn('de/aaschmid/test') ] as Set
    }

    def "applying 'CpdPlugin', 'JavaPlugin' and 'GroovyPlugin' sets source with 'main' and 'test' sourceSets of Java and Groovy project"() {
        given:
        project.plugins.apply(GroovyPlugin)
        project.sourceSets{
            main{
                java.srcDir testFile('de/aaschmid/foo')
                groovy.srcDir testFile('de/aaschmid/clazz')
            }
        }

        def cpdTask = project.tasks.getByName('cpdCheck')

        expect:
        cpdTask.source.files == [ *testFilesRecurseIn('de/aaschmid/foo'), *testFilesRecurseIn('de/aaschmid/clazz') ] as Set
    }

    def "applying 'JavaBasePlugin' and 'CpdPlugin' adds dependency to check task and configures source"() {
        given:
        Project project = ProjectBuilder.builder().build()

        project.plugins.apply(JavaBasePlugin)
        project.plugins.apply(CpdPlugin)

        project.sourceSets{
            tmp.java.srcDir testFile('')
        }

        def checkTask = project.tasks.getByName('check')
        def cpdTask = project.tasks.getByName('cpdCheck')

        expect:
        checkTask.taskDependencies.getDependencies(checkTask).find{ Task task ->
            task == cpdTask
        }
        cpdTask.source.files == testFilesRecurseIn('') as Set
    }

    def "'Cpd' task can be customized via extension"() {
        given:
        project.cpd{
            encoding = 'UTF-8'
            ignoreAnnotations = true
            ignoreFailures = true
            language = 'ruby'
            minimumTokenCount = 25
            reportsDir = project.file('cpd-reports')
            skipDuplicateFiles = true
            skipLexicalErrors = true
            skipBlocks = false
        }

        def task = project.tasks.getByName('cpdCheck')

        expect:
        task instanceof Cpd
        task.description == 'Run CPD analysis for all sources'
        task.group == null

        task.encoding == 'UTF-8'
        task.ignoreAnnotations
        task.ignoreFailures
        !task.ignoreIdentifiers
        !task.ignoreLiterals
        task.language == 'ruby'
        task.minimumTokenCount == 25

        task.pmdClasspath == project.configurations.findByName('cpd')

        task.reports.csv.destination == project.file('cpd-reports/cpdCheck.csv')
        !task.reports.csv.enabled
        task.reports.text.destination == project.file('cpd-reports/cpdCheck.text')
        !task.reports.text.enabled
        task.reports.xml.destination == project.file('cpd-reports/cpdCheck.xml')
        task.reports.xml.enabled

        task.skipDuplicateFiles
        task.skipLexicalErrors
        !task.skipBlocks
        task.skipBlocksPattern == Tokenizer.DEFAULT_SKIP_BLOCKS_PATTERN

        task.source.empty
    }

    def "'Cpd' task can be customized via 'cpdCheck' task"() {
        given:
        project.cpdCheck{
            encoding = 'ISO-8859-1'
            ignoreFailures = false
            ignoreIdentifiers = false
            ignoreLiterals = true
            language = 'cpp'
            minimumTokenCount = 10
            reports{
                csv{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpdCheck.csv")
                }
                text{
                    enabled = false
                    destination = project.file("${project.buildDir}/cpdCheck.text")
                }
                xml.enabled = false
            }
            skipDuplicateFiles = true
            skipLexicalErrors = true
            skipBlocks = false
            skipBlocksPattern = '<template|>'

            include '**.java'
            exclude '**Test*'
            source = project.file('src/')
        }

        def task = project.tasks.getByName('cpdCheck')

        expect:
        task instanceof Cpd
        task.description == 'Run CPD analysis for all sources'
        task.group == null

        task.encoding == 'ISO-8859-1'
        !task.ignoreAnnotations
        !task.ignoreFailures
        !task.ignoreIdentifiers
        task.ignoreLiterals
        task.language == 'cpp'
        task.minimumTokenCount == 10

        task.pmdClasspath == project.configurations.findByName('cpd')

        task.reports.csv.destination == project.file('build/cpdCheck.csv')
        task.reports.csv.enabled
        task.reports.text.destination == project.file('build/cpdCheck.text')
        !task.reports.text.enabled
        task.reports.xml.destination == project.file('build/reports/cpd/cpdCheck.xml')
        !task.reports.xml.enabled

        !task.skipBlocks
        task.skipBlocksPattern == '<template|>'

        task.source.empty
    }

    def "custom 'Cpd' task can be customized via extension if task is created after extension is configured"() {
        given:
        project.cpd{
            language = 'php'
            minimumTokenCount = 250
        }

        def task = project.tasks.create('cpdCustom', Cpd)

        expect:
        task.language == 'php'
        task.minimumTokenCount == 250
    }

    def "applying 'Cpd' task to only parent project should also add sources of sub-projects"() {
        given:
        def project = ProjectBuilder.builder().build()

        def subProject1 = ProjectBuilder.builder().withName('sub1').withParent(project).build()
        subProject1.file('src/main/java').mkdirs()
        subProject1.file('src/main/java/Clazz.java').createNewFile()
        subProject1.file('src/test/java').mkdirs()
        subProject1.file('src/test/java/ClazzTest.java').createNewFile()
        subProject1.plugins.apply(JavaPlugin)

        def subProject2 = ProjectBuilder.builder().withName('sub2').withParent(project).build()
        subProject2.file('src/main/groovy').mkdirs()
        subProject2.file('src/main/groovy/Clazz.groovy').createNewFile()
        subProject2.file('src/main/resources').mkdirs()
        subProject2.file('src/main/resources/clazz.properties').createNewFile()
        subProject2.plugins.apply(GroovyPlugin)

        when:
        project.plugins.apply(CpdPlugin)

        then:
        def task = project.tasks.getByName('cpdCheck')
        task.source.files == [
                subProject1.file('src/main/java/Clazz.java'),
                subProject1.file('src/test/java/ClazzTest.java'),
                subProject2.file('src/main/groovy/Clazz.groovy')
        ] as Set

        !subProject1.tasks.findByName('cpdCheck')
        !subProject2.tasks.findByName('cpdCheck')
    }
}
