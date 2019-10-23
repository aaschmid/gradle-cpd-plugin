package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.io.Serializable;

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

    ListProperty<Report> getReportParameters();


    abstract class Report implements Serializable {
        private final String encoding;
        private final File destination;

        Report(String encoding, File destination) {
            this.encoding = encoding;
            this.destination = destination;
        }

        String getEncoding() {
            return encoding;
        }

        File getDestination() {
            return destination;
        }

        public static class Csv extends Report {
            private final Character separator;

            public Csv(String encoding, File destination, Character separator) {
                super(encoding, destination);
                this.separator = separator;
            }

            Character getSeparator() {
                return separator;
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

            boolean getTrimLeadingCommonSourceWhitespaces() {
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
