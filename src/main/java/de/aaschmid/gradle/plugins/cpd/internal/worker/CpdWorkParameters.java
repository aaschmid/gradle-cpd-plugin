package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.util.List;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

public interface CpdWorkParameters extends WorkParameters {

    Property<String> getEncoding();

    Property<Boolean> getIgnoreAnnotations();

    Property<Boolean> getIgnoreFailures();

    Property<Boolean> getIgnoreIdentifiers();

    Property<Boolean> getIgnoreLiterals();

    Property<String> getLanguage();

    Property<Integer> getMinimumTokenCount();

    Property<Boolean> getSkipBlocks();

    Property<String> getSkipBlocksPattern();

    Property<Boolean> getSkipDuplicateFiles();

    Property<Boolean> getSkipLexicalErrors();

    ConfigurableFileCollection getSourceFiles();

    ListProperty<CpdReportParameters> getReportParameters();
}
