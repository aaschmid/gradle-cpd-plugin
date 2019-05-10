package de.aaschmid.gradle.plugins.cpd.internal.worker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.File;
import java.io.Serializable;

@RequiredArgsConstructor
@Getter
public class CpdReportConfiguration implements Serializable {

    private final String encoding;
    private final File destination;

    @Getter
    public static class CpdCsvReport extends CpdReportConfiguration {
        private final Character separator;

        public CpdCsvReport(String encoding, File destination, Character separator) {
            super(encoding, destination);
            this.separator = separator;
        }
    }

    @Getter
    public static class CpdTextReport extends CpdReportConfiguration {
        private final String lineSeparator;
        private final boolean trimLeadingCommonSourceWhitespaces;

        public CpdTextReport(String encoding, File destination, String lineSeparator, boolean trimLeadingCommonSourceWhitespaces) {
            super(encoding, destination);
            this.lineSeparator = lineSeparator;
            this.trimLeadingCommonSourceWhitespaces = trimLeadingCommonSourceWhitespaces;
        }
    }

    public static class CpdXmlReport extends CpdReportConfiguration {
        public CpdXmlReport(String encoding, File destination) {
            super(encoding, destination);
        }
    }
}
