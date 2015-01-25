package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import org.gradle.api.InvalidUserDataException

class CpdExecutorTest extends BaseSpec {

    def "test 'new CpdExecutor(null)' should throw 'NullPointerException'"() {
        when:
        new CpdExecutor(null)

        then:
        def e = thrown NullPointerException
        e.getMessage() ==~ /task must not be null/
    }

    def "test 'new CpdExecutor(...)' should throw 'InvalidUserDataException' if 'minimumTokenCount' is '-1'"() {
        given:
        project.cpdCheck{
            minimumTokenCount = -1
        }

        when:
        new CpdExecutor(project.cpdCheck)

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() ==~ /'minimumTokenCount' must be greater than zero./
    }

    def "test 'new CpdExecutor(...)' should get correct values from task"() {
        given:
        project.cpdCheck{
            encoding = 'US-ASCII'
            minimumTokenCount = 15
            source = testFile('de/aaschmid/clazz/Clazz.java')
        }

        when:
        def result = new CpdExecutor(project.cpdCheck)

        then:
        result.encoding == 'US-ASCII'
        result.minimumTokenCount == 15
        result.source.files == [ testFile('de/aaschmid/clazz/Clazz.java') ] as Set
    }
}
