package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import org.gradle.api.InvalidUserDataException

class CpdExecutorTest extends BaseSpec {

    def underTest

    def setup() {
        underTest = new CpdExecutor(tasks.cpd)
    }

    def "test 'new CpdExecutor(null)' should throw 'NullPointerException'"() {
        when:
        new CpdExecutor(null)

        then:
        def e = thrown NullPointerException
        e.getMessage() ==~ /task must not be null/
    }

    def "test 'canRun()' should throw 'InvalidUserDataException' if minimumTokenCount is '-1'"() {
        given:
        tasks.cpd{
            minimumTokenCount = -1
        }

        when:
        underTest.canRun()

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() ==~ /'minimumTokenCount' must be greater than zero./
    }
}
