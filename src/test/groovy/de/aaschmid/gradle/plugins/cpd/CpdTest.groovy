package de.aaschmid.gradle.plugins.cpd

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec

class CpdTest extends BaseSpec {

    def "'Cpd' task inputs are set correctly"() {
        given:
        project.cpdCheck{
            reports{
                text{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpdCheck.text")
                }
            }
            source = testFile('de/aaschmid/clazz/')
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.inputs.sourceFiles.files == project.files(
                testFile('de/aaschmid/clazz/Clazz.java'),
                testFile('de/aaschmid/clazz/impl/Clazz1.java'),
                testFile('de/aaschmid/clazz/impl/Clazz2.java'),
            ) as Set
        task.inputs.properties.size() == 13
    }

    def "'Cpd' task is aware of includes and excludes"() {
        given:
        project.cpdCheck{
            include '**/*.java'
            exclude '**/*1.java'
            exclude '**/*z.java'
            exclude '**/test/*'
            source = testFile('.')
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.inputs.sourceFiles.files == project.files(
                testFile('de/aaschmid/annotation/Employee.java'),
                testFile('de/aaschmid/annotation/Person.java'),
                testFile('de/aaschmid/clazz/impl/Clazz2.java'),
                testFile('de/aaschmid/duplicate/Test.java'),
                testFile('de/aaschmid/foo/Bar.java'),
                testFile('de/aaschmid/identifier/Identifier2.java'),
                testFile('de/aaschmid/lexical/Error.java'),
                testFile('de/aaschmid/literal/Literal2.java'),
            ) as Set
    }

    def "'Cpd' task outputs are set correctly"() {
        given:
        project.cpdCheck{
            reports{
                text{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpdCheck.text")
                }
            }
            source = testFile('.')
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.outputs.files.files == project.files("${project.buildDir}/cpdCheck.text", "${project.buildDir}/reports/cpd/cpdCheck.xml") as Set
    }

    def "'Cpd' task ignoreFailures is 'false' by default"() {
        expect:
        !project.tasks.findByName('cpdCheck').ignoreFailures
    }
}
