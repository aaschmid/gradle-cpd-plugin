package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.io.Serializable;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

import static java.util.Objects.requireNonNull;

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

    ListProperty<Report> getReportParameters();


    abstract class Report implements Serializable {
        private final String encoding;
        private final File destination;

        Report(String encoding, File destination) {
            this.encoding = requireNonNull(encoding, "'encoding' must not be null for any report.");
            this.destination = requireNonNull(destination, "'destination' must not be null for any report.");
        }

        String getEncoding() {
            return encoding;
        }

        File getDestination() {
            return destination;
        }

        public static class Csv extends Report {
            private final Character separator;
            private final boolean includeLineCount;

            public Csv(String encoding, File destination, Character separator, boolean includeLineCount) {
                super(encoding, destination);
                this.separator = separator;
                this.includeLineCount = includeLineCount;
            }

            Character getSeparator() {
                return separator;
            }

            public boolean isIncludeLineCount() {
                return includeLineCount;
            }
        }

        public static class Text extends Report {
            private final String lineSeparator;
            private final boolean trimLeadingCommonSourceWhitespaces;

            public Text(String encoding, File destination, String lineSeparator, boolean trimLeadingCommonSourceWhitespaces) {
                super(encoding, destination);
                this.lineSeparator = lineSeparator;
                this.trimLeadingCommonSourceWhitespaces = trimLeadingCommonSourceWhitespaces;
            }

            String getLineSeparator() {
                return lineSeparator;
            }

            boolean isTrimLeadingCommonSourceWhitespaces() {
                return trimLeadingCommonSourceWhitespaces;
            }
        }

        public static class Xml extends Report {
            public Xml(String encoding, File destination) {
                super(encoding, destination);
            }
        }
    }
}
