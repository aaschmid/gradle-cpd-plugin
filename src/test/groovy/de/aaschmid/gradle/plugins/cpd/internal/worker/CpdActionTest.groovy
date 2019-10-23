package de.aaschmid.gradle.plugins.cpd.internal.worker


import de.aaschmid.gradle.plugins.cpd.test.BaseSpec

class CpdActionTest extends BaseSpec {

//    def "test 'new CpdAction(...)' should get correct values from task including defaults"() {
//        given:
//        def config = new CpdExecutionConfiguration(
//                'US-ASCII',
//                false,
//                false,
//                false,
//                false,
//                'kotlin',
//                15,
//                false,
//                "",
//                false,
//                true,
//                [ testFile('de/aaschmid/clazz/Clazz.java') ] as Set
//            )
//
//        when:
//        def result = new CpdExecutor(config)
//
//        then:
//        result.cpdConfig.encoding == 'US-ASCII'
//        result.cpdConfig.language instanceof KotlinLanguage
//        result.cpdConfig.minimumTileSize == 15
//        !result.cpdConfig.skipDuplicates
//        result.cpdConfig.skipLexicalErrors
//        result.sourceFiles == [ testFile('de/aaschmid/clazz/Clazz.java') ] as Set
//    }
//
//    def "test 'new CpdExecutor(...)' should set correct java properties"() {
//        given:
//        def config = new CpdExecutionConfiguration(
//                'US-ASCII',
//                true,
//                false,
//                false,
//                true,
//                'java',
//                15,
//                false,
//                "",
//                false,
//                true,
//                [ testFile('de/aaschmid/clazz/Clazz.java') ] as Set
//        )
//
//        when:
//        def result = new CpdExecutor(config)
//
//        then:
//        result.cpdConfig.language instanceof JavaLanguage
//
//        def tokenizer = result.cpdConfig.language.tokenizer
//        tokenizer.ignoreAnnotations
//        !tokenizer.ignoreIdentifiers
//        tokenizer.ignoreLiterals
//    }
//
//    def "test 'new CpdExecutor(...)' should set correct cpp properties"() {
//        given:
//        def config = new CpdExecutionConfiguration(
//                'US-ASCII',
//                true,
//                false,
//                false,
//                true,
//                'cpp',
//                15,
//                true,
//                "template<|>",
//                false,
//                true,
//                [ testFile('de/aaschmid/clazz/Clazz.java') ] as Set
//        )
//
//        when:
//        def result = new CpdExecutor(config)
//
//        then:
//        result.cpdConfig.language instanceof CPPLanguage
//
//        def tokenizer = result.cpdConfig.language.tokenizer
//        tokenizer.skipBlocks
//        tokenizer.skipBlocksStart == 'template<'
//        tokenizer.skipBlocksEnd == '>'
//    }
}
