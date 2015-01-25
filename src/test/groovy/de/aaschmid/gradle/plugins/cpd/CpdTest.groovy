package de.aaschmid.gradle.plugins.cpd

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec

class CpdTest extends BaseSpec {

    def "'Cpd' task inputs are set correctly"() {
        given:
        project.tasks.cpd{
            reports{
                text{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpd.text")
                }
            }
            source = testFile('de/aaschmid/clazz/')
        }

        def task = project.tasks.findByName('cpd')

        expect:
        task.inputs.sourceFiles.files == project.files(
                testFile('de/aaschmid/clazz/Clazz.java'),
                testFile('de/aaschmid/clazz/impl/Clazz1.java'),
                testFile('de/aaschmid/clazz/impl/Clazz2.java'),
            ) as Set
        task.inputs.properties.size() == 5
    }

    def "'Cpd' task is aware of includes and excludes"() {
        given:
        project.tasks.cpd{
            include '**/*.java'
            exclude '**/*1.java'
            exclude '**/*z.java'
            source = testFile('.')
        }

        def task = project.tasks.findByName('cpd')

        expect:
        task.inputs.sourceFiles.files == project.files(
                testFile('de/aaschmid/clazz/impl/Clazz2.java'),
                testFile('de/aaschmid/foo/Bar.java'),
                testFile('de/aaschmid/test/Test.java'),
            ) as Set
    }

    def "'Cpd' task outputs are set correctly"() {
        given:
        project.tasks.cpd{
            reports{
                text{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpd.text")
                }
            }
            source = testFile('.')
        }

        def task = project.tasks.findByName('cpd')

        expect:
        task.outputs.files.files == project.files("${project.buildDir}/cpd.text", "${project.buildDir}/reports/cpd/cpd.xml") as Set
    }
}
