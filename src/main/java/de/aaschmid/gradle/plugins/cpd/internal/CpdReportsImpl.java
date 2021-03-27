package de.aaschmid.gradle.plugins.cpd.internal;

import javax.inject.Inject;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport;
import de.aaschmid.gradle.plugins.cpd.CpdReports;
import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import de.aaschmid.gradle.plugins.cpd.CpdXmlFileReport;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;
import org.gradle.api.reporting.internal.TaskReportContainer;

public class CpdReportsImpl extends TaskReportContainer<SingleFileReport> implements CpdReports {

    @Inject
    public CpdReportsImpl(Cpd task, CollectionCallbackActionDecorator callbackActionDecorator) {
        super(SingleFileReport.class, task, callbackActionDecorator);

        add(CpdCsvFileReportImpl.class, "csv", task);
        add(CpdTextFileReportImpl.class, "text", task);
        add(TaskGeneratedSingleFileReport.class, "vs", task);
        add(CpdXmlFileReportImpl.class, "xml", task);
    }

    @Override
    public CpdCsvFileReport getCsv() {
        return (CpdCsvFileReport) getByName("csv");
    }

    @Override
    public CpdTextFileReport getText() {
        return (CpdTextFileReport) getByName("text");
    }

    @Override
    public SingleFileReport getVs() {
        return getByName("vs");
    }

    @Override
    public CpdXmlFileReport getXml() {
        return (CpdXmlFileReport) getByName("xml");
    }
}
