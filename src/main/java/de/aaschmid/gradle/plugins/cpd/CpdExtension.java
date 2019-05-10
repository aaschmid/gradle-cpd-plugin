package de.aaschmid.gradle.plugins.cpd;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.plugins.quality.CodeQualityExtension;


/**
 * Configuration options for the CPD plugin.
 * <p>
 * The sample below shows various configuration options:
 *
 * <pre>
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
@Getter
@Setter
public class CpdExtension extends CodeQualityExtension {

    /**
     * The character set encoding (e.g., UTF-8) to use when reading the source code files but also when producing the
     * report; defaults to {@code System.getProperty("file.encoding")}.
     * <p>
     * Example: {@code encoding = UTF-8}
     *
     * @return the charset encoding
     */
    private String encoding = System.getProperty("file.encoding");

    /**
     * Ignore annotations because more and more modern frameworks use annotations on classes and methods which can be
     * very redundant and causes false positives; defaults to {@code false}.
     * <p>
     * Note: if this option is recognized depends on the used {@link #language} and its tokenizer.
     * <p>
     * Example: {@code ignoreAnnotations = true}
     *
     * @return whether annotation should be ignored
     */
    private boolean ignoreAnnotations = false;

    /**
     * Option if CPD should ignore identifiers differences, i.e. variable names, methods names, and so forth, when
     * evaluating a duplicate block; defaults to {@code false}.
     * <p>
     * Note: if this option is recognized depends on the used {@link #language} and its tokenizer.
     * <p>
     * Example: {@code ignoreIdentifiers = true}
     *
     * @return whether identifiers should be ignored
     */
    private boolean ignoreIdentifiers = false;

    /**
     * Option if CPD should ignore literal value differences when evaluating a duplicate block; defaults to {@code
     * false}. This means e.g. that {@code foo=42;} and {@code foo=43;} will be seen as equivalent.
     * <p>
     * Note: if this option is recognized depends on the used {@link #language} and its tokenizer.
     * <p>
     * Example: {@code ignoreLiterals = true}
     *
     * @return whether literals should be ignored
     */
    private boolean ignoreLiterals = false;

    /**
     * Flag to select the appropriate language; defaults to {@code 'java'}.
     * <p>
     * Example: {@code language = 'java'}
     *
     * @return the used language
     */
    private String language = "java";

    /**
     * A positive integer indicating the minimum token count to trigger a CPD match; defaults to {@code 50}.
     * <p>
     * Example: {@code minimumTokenCount = 25}
     *
     * @return the minimum token count
     */
    private int minimumTokenCount = 50;

    /**
     * Ignore multiple copies of files of the same name and length in comparison; defaults to {@code false}.
     * <p>
     * Example: {@code skipDuplicateFiles = true}
     *
     * @return whether duplicate files should be skipped
     */
    private boolean skipDuplicateFiles = false;

    /**
     * Skip files which cannot be tokenized due to invalid characters instead of aborting CPD; defaults to {@code
     * false}.
     * <p>
     * Example: {@code skipLexicalErrors = true}
     *
     * @return whether lexical errors should be skipped
     */
    private boolean skipLexicalErrors = false;

    /**
     * Enables or disables skipping of blocks configured by {@link #skipBlocksPattern} like a pre-processor; defaults to
     * {@code true}.
     * <p>
     * Example: {@code skipBlocks = false}
     *
     * @see #skipBlocksPattern
     *
     * @return whether blocks should be skipped by a given pattern
     */
    private boolean skipBlocks = true;

    /**
     * CConfigures the pattern, to find the blocks to skip if enabled using {@link #skipBlocks}. It is a {@link String}
     * property and contains of two parts, separated by {@code '|'}. The first part is the start pattern, the second
     * part is the ending pattern. defaults to {@code '#if 0|#endif'} (which should be the same as {@link
     * net.sourceforge.pmd.cpd.Tokenizer#DEFAULT_SKIP_BLOCKS_PATTERN}).
     * <p>
     * Example: {@code skipBlocksPattern = '#include <|>'}
     *
     * @see #skipBlocks
     *
     * @return the pattern used to skip blocks
     */
    private String skipBlocksPattern = "#if 0|#endif";
}
