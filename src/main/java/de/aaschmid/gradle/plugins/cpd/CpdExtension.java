package de.aaschmid.gradle.plugins.cpd;

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
public class CpdExtension extends CodeQualityExtension {

    private String encoding;
    private boolean ignoreAnnotations = false;
    private boolean ignoreIdentifiers = false;
    private boolean ignoreLiterals = false;
    private String language = "java";
    private int minimumTokenCount = 50;
    private boolean skipDuplicateFiles = false;
    private boolean skipLexicalErrors = false;
    private boolean skipBlocks = true;
    private String skipBlocksPattern = "#if 0|#endif";

    /**
     * The character set encoding (e.g., UTF-8) to use when reading the source code files but also when producing the report; defaults to
     * {@code System.getProperty("file.encoding")}.
     * <p>
     * Example: {@code encoding = UTF-8}
     *
     * @return the charset encoding
     */
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Ignore annotations because more and more modern frameworks use annotations on classes and methods which can be very redundant and
     * causes false positives; defaults to {@code false}.
     * <p>
     * Note: if this option is recognized depends on the used {@link #language} and its tokenizer.
     * <p>
     * Example: {@code ignoreAnnotations = true}
     *
     * @return whether annotation should be ignored
     */
    public boolean isIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    public void setIgnoreAnnotations(boolean ignoreAnnotations) {
        this.ignoreAnnotations = ignoreAnnotations;
    }

    /**
     * Option if CPD should ignore identifiers differences, i.e. variable names, methods names, and so forth, when evaluating a duplicate
     * block; defaults to {@code false}.
     * <p>
     * Note: if this option is recognized depends on the used {@link #language} and its tokenizer.
     * <p>
     * Example: {@code ignoreIdentifiers = true}
     *
     * @return whether identifiers should be ignored
     */
    public boolean isIgnoreIdentifiers() {
        return ignoreIdentifiers;
    }

    public void setIgnoreIdentifiers(boolean ignoreIdentifiers) {
        this.ignoreIdentifiers = ignoreIdentifiers;
    }

    /**
     * Option if CPD should ignore literal value differences when evaluating a duplicate block; defaults to {@code false}. This means e.g.
     * that {@code foo=42;} and {@code foo=43;} will be seen as equivalent.
     * <p>
     * Note: if this option is recognized depends on the used {@link #language} and its tokenizer.
     * <p>
     * Example: {@code ignoreLiterals = true}
     *
     * @return whether literals should be ignored
     */
    public boolean isIgnoreLiterals() {
        return ignoreLiterals;
    }

    public void setIgnoreLiterals(boolean ignoreLiterals) {
        this.ignoreLiterals = ignoreLiterals;
    }

    /**
     * Flag to select the appropriate language; defaults to {@code 'java'}.
     * <p>
     * Example: {@code language = 'java'}
     *
     * @return the used language
     */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * A positive integer indicating the minimum token count to trigger a CPD match; defaults to {@code 50}.
     * <p>
     * Example: {@code minimumTokenCount = 25}
     *
     * @return the minimum token count
     */
    public int getMinimumTokenCount() {
        return minimumTokenCount;
    }

    public void setMinimumTokenCount(int minimumTokenCount) {
        this.minimumTokenCount = minimumTokenCount;
    }

    /**
     * Ignore multiple copies of files of the same name and length in comparison; defaults to {@code false}.
     * <p>
     * Example: {@code skipDuplicateFiles = true}
     *
     * @return whether duplicate files should be skipped
     */
    public boolean isSkipDuplicateFiles() {
        return skipDuplicateFiles;
    }

    public void setSkipDuplicateFiles(boolean skipDuplicateFiles) {
        this.skipDuplicateFiles = skipDuplicateFiles;
    }

    /**
     * Skip files which cannot be tokenized due to invalid characters instead of aborting CPD; defaults to {@code false}.
     * <p>
     * Example: {@code skipLexicalErrors = true}
     *
     * @return whether lexical errors should be skipped
     */
    public boolean isSkipLexicalErrors() {
        return skipLexicalErrors;
    }

    public void setSkipLexicalErrors(boolean skipLexicalErrors) {
        this.skipLexicalErrors = skipLexicalErrors;
    }

    /**
     * Enables or disables skipping of blocks configured by {@link #skipBlocksPattern} like a pre-processor; defaults to {@code true}.
     * <p>
     * Example: {@code skipBlocks = false}
     *
     * @return whether blocks should be skipped by a given pattern
     * @see #skipBlocksPattern
     */
    public boolean isSkipBlocks() {
        return skipBlocks;
    }

    public void setSkipBlocks(boolean skipBlocks) {
        this.skipBlocks = skipBlocks;
    }

    /**
     * CConfigures the pattern, to find the blocks to skip if enabled using {@link #skipBlocks}. It is a {@link String} property and
     * contains of two parts, separated by {@code '|'}. The first part is the start pattern, the second part is the ending pattern. defaults
     * to {@code '#if 0|#endif'} (which should be the same as {@link net.sourceforge.pmd.cpd.internal.CpdLanguagePropertiesDefaults#DEFAULT_SKIP_BLOCKS_PATTERN}).
     * <p>
     * Example: {@code skipBlocksPattern = '#include <|>'}
     *
     * @return the pattern used to skip blocks
     * @see #skipBlocks
     */
    public String getSkipBlocksPattern() {
        return skipBlocksPattern;
    }

    public void setSkipBlocksPattern(String skipBlocksPattern) {
        this.skipBlocksPattern = skipBlocksPattern;
    }
}
