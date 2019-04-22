package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.io.Serializable;

public class CpdReportConfiguration implements Serializable {

    private final String encoding;
    private final File destination;

    CpdReportConfiguration(String encoding, File destination) {
        this.encoding = encoding;
        this.destination = destination;
    }

    String getEncoding() {
        return encoding;
    }

    File getDestination() {
        return destination;
    }

    public static class CpdCsvReport extends CpdReportConfiguration {
        private final Character separator;

        public CpdCsvReport(String encoding, File destination, Character separator) {
            super(encoding, destination);
            this.separator = separator;
        }

        Character getSeparator() {
            return separator;
        }
    }

    public static class CpdTextReport extends CpdReportConfiguration {
        private final String lineSeparator;
        private final boolean trimLeadingCommonSourceWhitespaces;

        public CpdTextReport(String encoding, File destination, String lineSeparator, boolean trimLeadingCommonSourceWhitespaces) {
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

    public static class CpdXmlReport extends CpdReportConfiguration {
        public CpdXmlReport(String encoding, File destination) {
            super(encoding, destination);
        }
    }
}
