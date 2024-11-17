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

    Property<Boolean> getFailOnError();

    Property<Boolean> getFailOnViolation();

    Property<Boolean> getIgnoreAnnotations();

    Property<Boolean> getIgnoreFailures();

    Property<Boolean> getIgnoreIdentifiers();

    Property<Boolean> getIgnoreLiterals();

    Property<String> getLanguage();

    Property<Integer> getMinimumTokenCount();

    Property<Boolean> getSkipBlocks();

    Property<String> getSkipBlocksPattern();

    Property<Boolean> getSkipDuplicateFiles();

    ConfigurableFileCollection getSourceFiles();

    ListProperty<Report> getReportParameters();


    abstract class Report implements Serializable {
        private final File destination;

        Report(File destination) {
            this.destination = requireNonNull(destination, "'destination' must not be null for any report.");
        }

        File getDestination() {
            return destination;
        }

        public static class Csv extends Report {
            private final Character separator;
            private final boolean includeLineCount;

            public Csv(File destination, Character separator, boolean includeLineCount) {
                super(destination);
                this.separator = separator;
                this.includeLineCount = includeLineCount;
            }

            Character getSeparator() {
                return separator;
            }

            boolean isIncludeLineCount() {
                return includeLineCount;
            }
        }

        public static class Text extends Report {
            private final String lineSeparator;
            private final boolean trimLeadingCommonSourceWhitespaces;

            public Text(File destination, String lineSeparator, boolean trimLeadingCommonSourceWhitespaces) {
                super(destination);
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

        public static class Vs extends Report {
            public Vs(File destination) {
                super(destination);
            }
        }

        public static class Xml extends Report {
            private final String encoding;

            public Xml(File destination, String encoding) {
                super(destination);
                this.encoding = encoding;
            }

            String getEncoding() {
                return encoding;
            }
        }
    }
}
