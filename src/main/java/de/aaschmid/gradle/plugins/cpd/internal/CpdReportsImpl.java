package de.aaschmid.gradle.plugins.cpd.internal;

import javax.inject.Inject;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport;
import de.aaschmid.gradle.plugins.cpd.CpdReports;
import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import de.aaschmid.gradle.plugins.cpd.CpdXmlFileReport;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reporting.SingleFileReport;

public class CpdReportsImpl extends Reports<SingleFileReport> implements CpdReports {

    @Inject
    public CpdReportsImpl(Cpd task, ObjectFactory objects) {
        super(task.getProject(), SingleFileReport.class);

        final CpdCsvFileReportImpl csv = objects.newInstance(CpdCsvFileReportImpl.class, "csv", task);
        final CpdTextFileReportImpl text = objects.newInstance(CpdTextFileReportImpl.class, "text", task);
        final CpdReportInternal vs = objects.newInstance(CpdReportInternal.class, "vs", task);
        final CpdXmlFileReportImpl xml = objects.newInstance(CpdXmlFileReportImpl.class, "xml", task);

        addReport(csv);
        addReport(text);
        addReport(vs);
        addReport(xml);
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

    @Override
    public void csv(Action<CpdCsvFileReport> action) {
        action.execute(getCsv());
    }

    @Override
    public void text(Action<CpdTextFileReport> action) {
        action.execute(getText());
    }

    @Override
    public void vs(Action<SingleFileReport> action) {
        action.execute(getVs());
    }

    @Override
    public void xml(Action<CpdXmlFileReport> action) {
        action.execute(getXml());
    }
}
