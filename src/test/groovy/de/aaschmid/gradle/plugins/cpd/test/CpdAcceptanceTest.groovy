package de.aaschmid.gradle.plugins.cpd.test

import de.aaschmid.gradle.plugins.cpd.CpdPlugin
import org.apache.commons.io.output.TeeOutputStream
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.ExternalResource

class CpdAcceptanceTest extends BaseSpec {

    @Rule
    public final ExternalResource logCapture = new ExternalResource() {

        private static final String ENCODING = "UTF-8";

        private final ByteArrayOutputStream log = new ByteArrayOutputStream();
        private PrintStream originalStdOut;

        @Override
        protected void before() throws Throwable {
            originalStdOut = System.out;
            System.setOut(new PrintStream(new TeeOutputStream(originalStdOut, log), false, ENCODING));
        }

        @Override
        protected void after() {
            System.setOut(originalStdOut);
        }

        public String getLog() throws Exception {
            return log.toString(ENCODING);
        }
    }

    def setup() {
        project.repositories{
            mavenLocal()
            mavenCentral()
        }
    }

    def "'Cpd' task inputs are set correctly"() {
        given:
        project.cpdCheck{
            reports{
                text{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpd.text")
                }
            }
            source = testFile('de/aaschmid/clazz/')
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.inputs.files.filter{ file -> !file.name =~ /.java/ }.empty // TODO requires mavenLocal() etc.
    }

    // TODO Test incremental build feature? how?
    // TODO Test reports feature? how?

    def "'Cpd' task will be skipped if no source is set"() {
        given:
        project.cpdCheck{
            include '**/*.java'
            exclude '**/*1.java'
            exclude '**/*z.java'
            //source
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.execute()

        // TODO how to find out if task was executed or not?
        // TODO ask task graph?
    }

    def "'CpdPlugin' allows configuring tool dependencies explicitly via toolVersion property"() {
        given:
        project.cpd{ toolVersion '5.2.1' }
        project.cpdCheck{ source = testFile('.') }

        when:
        def result = project.configurations.cpd.resolve()

        then:
        result.any{ file -> file.name == 'pmd-core-5.2.1.jar' }
    }

    def "'CpdPlugin' allows configuring tool dependencies explicitly via configuration"() {
        given:
        project.dependencies{ cpd 'net.sourceforge.pmd:pmd:5.0.2' }
        project.cpdCheck{ source = testFile('.') }

        when:
        def result = project.configurations.cpd.resolve()

        then:
        result.any{ file -> file.name == 'pmd-5.0.2.jar' }
    }

    // TODO use pmd dependency if pmd plugin applied?

    def "executing 'Cpd' task throws wrapped 'InvalidUserDataException' if no report is enabled"() {
        given:
        project.cpdCheck{
            reports{
                csv.enabled = false
                text.enabled = false
                xml.enabled = false
            }
            source = testFile('.')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        !project.file('build/reports/cpdCheck.csv').exists()

        def e = thrown(TaskExecutionException)
        e.cause instanceof InvalidUserDataException
        e.cause.message == '''Task 'cpdCheck' requires exactly one report to be enabled but was: [].'''
    }

    def "executing 'Cpd' task throws wrapped 'InvalidUserDataException' if more than one report is enabled"() {
        given:
        project.cpdCheck{
            reports{
                csv.enabled = false
                text.enabled = true
                xml.enabled = true
            }
            source = testFile('.')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        !project.file('build/reports/cpdCheck.csv').exists()

        def e = thrown(TaskExecutionException)
        e.cause instanceof InvalidUserDataException
        e.cause.message == '''Task 'cpdCheck' requires exactly one report to be enabled but was: [text, xml].'''
    }

    def "executing 'Cpd' task on non-duplicate 'java' source will produce empty 'cpdCheck.xml'"() {
        given:
        project.cpd{
            encoding = 'ISO-8859-1'
            minimumTokenCount = 10
        }
        project.cpdCheck.source = testFile('de/aaschmid/foo')

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        def report = project.file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        // TODO do better?
        report.text =~ /encoding="ISO-8859-1"/
        report.text =~ /<pmd-cpd\/>/
    }

    def "executing 'Cpd' task on duplicate 'java' source should throw 'GradleException' and produce 'cpdCheck.csv' with one warning"() {
        given:
        project.cpdCheck{
            minimumTokenCount = 15
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('de/aaschmid/clazz')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        def e = thrown GradleException
        e.cause.message =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = project.file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /4,15,2,[79],.*Clazz[12]\.java,[79],.*Clazz[12]\.java/
    }

    def "executing 'Cpd' task on duplicate 'java' source should not throw 'GradleException' if 'ignoreFailures' and produce 'cpdCheck.csv' with one warning"() {
        given:
        project.cpdCheck{
            ignoreFailures = true
            minimumTokenCount = 15
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('de/aaschmid/clazz')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        notThrown GradleException

        def report = project.file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /4,15,2,[79],.*Clazz[12]\.java,[79],.*Clazz[12]\.java/
    }

    def "applying 'Cpd' task to only parent project if only sub project has 'groovy' plugin"() {
        given:
        project = ProjectBuilder.builder().build()
        project.repositories{
            mavenLocal()
            mavenCentral()
        }
        def subProject = ProjectBuilder.builder().withParent(project).build()

        project.plugins.apply(CpdPlugin)

        project.cpdCheck{
            ignoreFailures = true
            minimumTokenCount = 2
        }

        subProject.plugins.apply(GroovyPlugin)
        subProject.sourceSets{
            main{
                java.srcDir testFile('de/aaschmid/foo')
                groovy.srcDir testFile('de/aaschmid/clazz')
            }
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        (testFilesRecurseIn('de/aaschmid/foo') + testFilesRecurseIn('de/aaschmid/clazz')).each{ f ->
            logCapture.log.contains("Tokenizing ${f.path}")
        }

        def report = project.file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        report.text =~ /Bar.java/
        report.text =~ /Baz.java/
        report.text =~ /Clazz.java/
        report.text =~ /Clazz1.java/
        report.text =~ /Clazz2.java/
    }

    def "executing 'Cpd' task on duplicate annotations should throw 'GradleException' if not ignoreAnnotations"() {
        given:
        project.cpdCheck{
            ignoreAnnotations = false
            minimumTokenCount = 40
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('de/aaschmid/annotation')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        def e = thrown GradleException
        e.cause.message =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = project.file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        // locally Person.java comes before Employee, on travis-ci is Employee first => make it irrelevant
        report.text =~ /8,53,2,6,.*(Person|Employee)\.java,6,.*(Person|Employee)\.java/
        report.text =~ /14,45,2,13,.*(Person|Employee)\.java,13,.*(Person|Employee)\.java/
    }

    def "executing 'Cpd' task on duplicate annotations should not throw 'GradleException' if ignoreAnnotations"() {
        given:
        project.cpdCheck{
            ignoreAnnotations = true
            minimumTokenCount = 40
            source = testFile('de/aaschmid/annotation')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        notThrown GradleException

        def report = project.file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        // TODO do better?
        report.text =~ /<pmd-cpd\/>/
    }

    def "executing 'Cpd' task on different identifiers should throw 'GradleException' if ignoreIdentifiers"() {
        given:
        project.cpdCheck{
            ignoreIdentifiers = true
            minimumTokenCount = 15
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('de/aaschmid/identifier')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        def e = thrown GradleException
        e.cause.message =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = project.file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /6,19,2,3,.*Identifier[12]\.java,3,.*Identifier[12]\.java/
    }

    def "executing 'Cpd' task on different annotations should not throw 'GradleException' if not ignoreIdentifiers"() {
        given:
        project.cpdCheck{
            ignoreIdentifiers = false
            minimumTokenCount = 15
            source = testFile('de/aaschmid/identifier')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        notThrown GradleException

        def report = project.file('build/reports/cpd/cpdCheck.xml') // TODO file exists always; same as for other tools?
        report.exists()
        // TODO do better?
        report.text =~ /<pmd-cpd\/>/
    }

    def "executing 'Cpd' task on different literals should throw 'GradleException' if ignoreLiterals"() {
        given:
        project.cpdCheck{
            ignoreLiterals = true
            minimumTokenCount = 20
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('de/aaschmid/literal')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        def e = thrown GradleException
        e.cause.message =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = project.file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /9,27,2,5,.*Literal[12]\.java,5,.*Literal[12]\.java/
    }

    def "executing 'Cpd' task on different literals should not throw 'GradleException' if not ignoreLiterals"() {
        given:
        project.cpdCheck{
            ignoreLiterals = false
            minimumTokenCount = 20
            source = testFile('de/aaschmid/literal')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        notThrown GradleException

        def report = project.file('build/reports/cpd/cpdCheck.xml') // TODO file exists always; same as for other tools?
        report.exists()
        // TODO do better?
        report.text =~ /<pmd-cpd\/>/
    }

    def "executing 'Cpd' task on duplicate files should throw 'GradleException' if not skipDuplicateFiles"() {
        given:
        project.cpdCheck{
            minimumTokenCount = 5
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            skipDuplicateFiles = false
            source testFile('de/aaschmid/duplicate'), testFile('de/aaschmid/test')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        def e = thrown GradleException
        e.cause.message =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = project.file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /6,15,2,5,.*(duplicate|test)\/Test\.java,5,.*(duplicate|test)\/Test\.java/
    }

    def "executing 'Cpd' task on duplicate files should not throw 'GradleException' if skipDuplicateFiles"() {
        given:
        project.cpdCheck{
            minimumTokenCount = 5
            skipDuplicateFiles = true
            source testFile('de/aaschmid/duplicate'), testFile('de/aaschmid/test')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        notThrown GradleException

        def report = project.file('build/reports/cpd/cpdCheck.xml') // TODO file exists always; same as for other tools?
        report.exists()
        // TODO do better?
        report.text =~ /<pmd-cpd\/>/
    }


    def "executing 'Cpd' task on files containing lexical errors should throw 'GradleException' if not skipLexicalErrors"() {
        given:
        project.cpdCheck{
            skipLexicalErrors = false
            source testFile('de/aaschmid/lexical')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        def e = thrown GradleException
        e.cause.message =~ /Lexical error in file .*Error.java at/

        def report = project.file('build/reports/cpd/cpdCheck.csv')
        !report.exists()
    }

    def "executing 'Cpd' task on files containing lexical errors should not throw 'GradleException' if skipLexicalErrors"() {
        given:
        project.cpdCheck{
            skipLexicalErrors = true
            source testFile('de/aaschmid/lexical')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        notThrown GradleException

        def report = project.file('build/reports/cpd/cpdCheck.xml') // TODO file exists always; same as for other tools?
        report.exists()
        // TODO do better?
        report.text =~ /<pmd-cpd\/>/
    }

    // TODO further tests
}
