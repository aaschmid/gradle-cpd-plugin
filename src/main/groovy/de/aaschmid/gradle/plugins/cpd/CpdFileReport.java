package de.aaschmid.gradle.plugins.cpd;

import net.sourceforge.pmd.cpd.Renderer;
import org.gradle.api.Task;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public interface CpdFileReport {

    /**
     * @return a full configured {@link Renderer} to create a duplication report for CPD.
     */
    Renderer createRenderer();
}
