package de.aaschmid.gradle.plugins.cpd

import org.gradle.api.Project
import org.gradle.api.plugins.quality.CodeQualityExtension


/**
 * Configuration options for the CPD plugin.
 * <p>
 * The sample below shows various configuration options:
 *
 * <pre autoTested=''>
 * apply plugin: 'cpd'
 *
 * cpd {
 *     encoding = 'UTF-8'
 *     ignoreAnnotations = true
 *     language = 'java'
 *     minimumTokenCount = 20
 * }
 * </pre>
 *
 * @see CpdPlugin
 */
class CpdExtension extends CodeQualityExtension {

    private final Project project

    CpdExtension(Project project) {
        this.project = project
    }

    /**
     * The character set encoding (e.g., UTF-8) to use when reading the source code files but also when producing the
     * report; defaults to {@code System.getProperty("file.encoding")}.
     * <p>
     * Example: {@code encoding = UTF-8}
     */
    String encoding = System.getProperty('file.encoding')

    /**
     * Ignore annotations because more and more modern frameworks use annotations on classes and methods which can be very
     * redundant and causes false positives; defaults to {@code false}.
     * <p>
     * Note: if this option is recognized depends on the used {@link #language} and its tokenizer.
     * <p>
     * Example: {@code ignoreAnnotations = true}
     */
    boolean ignoreAnnotations = false

    /**
     * Option if CPD should ignore identifiers differences, i.e. variable names, methods names, and so forth,
     * when evaluating a duplicate block; defaults to {@code false}.
     * <p>
     * Note: if this option is recognized depends on the used {@link #language} and its tokenizer.
     * <p>
     * Example: {@code ignoreIdentifiers = true}
     */
    boolean ignoreIdentifiers = false

    /**
     * Whether or not to allow the build to continue if there are warnings; defaults to {@code false}, as for any other
     * static code analysis tool.
     * <p>
     * Example: {@code ignoreFailures = true}
     */
    boolean ignoreFailures = false

    /**
     * Option if CPD should ignore literal value differences when evaluating a duplicate block; defaults to
     * {@code false}. This means e.g. that {@code foo=42;} and {@code foo=43;} will be seen as equivalent.
     * <p>
     * Note: if this option is recognized depends on the used {@link #language} and its tokenizer.
     * <p>
     * Example: {@code ignoreLiterals = true}
     */
    boolean ignoreLiterals = false

    /**
     * Flag to select the appropriate language; defaults to {@code 'java'}.
     * <p>
     * Example: {@code language = 'java'}
     */
    String language = 'java'

    /**
     * A positive integer indicating the minimum token count to trigger a CPD match; defaults to {@code 50}.
     * <p>
     * Example: {@code minimumTokenCount = 25}
     */
    int minimumTokenCount = 50

    /**
     * Enables or disables skipping of blocks configured by {@link #skipBlocksPattern} like a pre-processor;
     * defaults to {@code true}.
     * <p>
     * Example: {@code skipBlocks = false}
     * @see #skipBlocksPattern
     */
    boolean skipBlocks = true

    /**
     * CConfigures the pattern, to find the blocks to skip if enabled using {@link #skipBlocks}. It is a {@link String}
     * property and contains of two parts, separated by {@code '|'}. The first part is the start pattern, the second part
     * is the ending pattern. defaults to {@code '#if 0|#endif'} (which should be the same as
     * {@link net.sourceforge.pmd.cpd.Tokenizer#DEFAULT_SKIP_BLOCKS_PATTERN}).
     * <p>
     * Example: {@code skipBlocksPattern = '#include <|>'}
     * @see #skipBlocks
     */
    String skipBlocksPattern = '#if 0|#endif'
}
