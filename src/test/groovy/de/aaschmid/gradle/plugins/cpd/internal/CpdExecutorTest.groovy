package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import net.sourceforge.pmd.cpd.CPPLanguage
import net.sourceforge.pmd.cpd.JavaLanguage
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

    def "test 'new CpdExecutor(...)' should get correct values from task including defaults"() {
        given:
        project.cpdCheck{
            encoding = 'US-ASCII'
            minimumTokenCount = 15
            skipLexicalErrors = true
            source = testFile('de/aaschmid/clazz/Clazz.java')
        }

        when:
        def result = new CpdExecutor(project.cpdCheck)

        then:
        result.encoding == 'US-ASCII'
        result.language instanceof JavaLanguage
        result.minimumTokenCount == 15
        !result.skipDuplicateFiles
        result.skipLexicalErrors
        result.source.files == [ testFile('de/aaschmid/clazz/Clazz.java') ] as Set
    }

    def "test 'new CpdExecutor(...)' should set correct java properties"() {
        given:
        project.cpdCheck{
            ignoreAnnotations = true
            ignoreIdentifiers = false
            ignoreLiterals = true
        }

        when:
        def result = new CpdExecutor(project.cpdCheck)

        then:
        result.language instanceof JavaLanguage

        def tokenizer = result.language.tokenizer
        tokenizer.ignoreAnnotations
        !tokenizer.ignoreIdentifiers
        tokenizer.ignoreLiterals
    }

    def "test 'new CpdExecutor(...)' should set correct cpp properties"() {
        given:
        project.cpdCheck{
            language = 'cpp'
            skipBlocks = true
            skipBlocksPattern = 'template<|>'
        }

        when:
        def result = new CpdExecutor(project.cpdCheck)

        then:
        result.language instanceof CPPLanguage

        def tokenizer = result.language.tokenizer
        tokenizer.skipBlocks
        tokenizer.skipBlocksStart == 'template<'
        tokenizer.skipBlocksEnd == '>'
    }
}
