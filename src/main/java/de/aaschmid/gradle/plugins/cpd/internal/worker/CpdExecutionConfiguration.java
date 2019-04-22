package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

public class CpdExecutionConfiguration implements Serializable {

    private final String encoding;
    private final boolean ignoreAnnotations;
    private final boolean ignoreFailures;
    private final boolean ignoreIdentifiers;
    private final boolean ignoreLiterals;
    private final String language;
    private final int minimumTokenCount;
    private final boolean skipBlocks;
    private final String skipBlocksPattern;
    private final boolean skipDuplicateFiles;
    private final boolean skipLexicalErrors;
    private final Set<File> sourceFiles;

    public CpdExecutionConfiguration(
            String encoding,
            boolean ignoreAnnotations,
            boolean ignoreFailures,
            boolean ignoreIdentifiers,
            boolean ignoreLiterals,
            String language,
            int minimumTokenCount,
            boolean skipBlocks,
            String skipBlocksPattern,
            boolean skipDuplicateFiles,
            boolean skipLexicalErrors,
            Set<File> sourceFiles
    ) {
        this.encoding = encoding;
        this.ignoreAnnotations = ignoreAnnotations;
        this.ignoreFailures = ignoreFailures;
        this.ignoreIdentifiers = ignoreIdentifiers;
        this.ignoreLiterals = ignoreLiterals;
        this.language = language;
        this.minimumTokenCount = minimumTokenCount;
        this.skipBlocks = skipBlocks;
        this.skipBlocksPattern = skipBlocksPattern;
        this.skipDuplicateFiles = skipDuplicateFiles;
        this.skipLexicalErrors = skipLexicalErrors;
        this.sourceFiles = sourceFiles;
    }

    String getEncoding() {
        return encoding;
    }

    boolean isIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    boolean isIgnoreFailures() {
        return ignoreFailures;
    }

    boolean isIgnoreIdentifiers() {
        return ignoreIdentifiers;
    }

    boolean isIgnoreLiterals() {
        return ignoreLiterals;
    }

    String getLanguage() {
        return language;
    }

    int getMinimumTokenCount() {
        return minimumTokenCount;
    }

    boolean isSkipBlocks() {
        return skipBlocks;
    }

    String getSkipBlocksPattern() {
        return skipBlocksPattern;
    }

    boolean isSkipDuplicateFiles() {
        return skipDuplicateFiles;
    }

    boolean isSkipLexicalErrors() {
        return skipLexicalErrors;
    }

    Set<File> getSourceFiles() {
        return sourceFiles;
    }
}
