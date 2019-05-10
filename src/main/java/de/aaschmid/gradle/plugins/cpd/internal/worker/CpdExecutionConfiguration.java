package de.aaschmid.gradle.plugins.cpd.internal.worker;

import lombok.Value;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

@Value
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

}
