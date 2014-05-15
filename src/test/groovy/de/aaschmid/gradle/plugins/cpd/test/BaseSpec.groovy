package de.aaschmid.gradle.plugins.cpd.test

import de.aaschmid.gradle.plugins.cpd.CpdPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

abstract class BaseSpec extends Specification {

    public Project project = ProjectBuilder.builder().build()

    def setup() {
        project.plugins.apply(CpdPlugin)
    }

    /**
     * Creates a {@link File} with location <code>classpath:/test-data/java/${relativePath}</code> as absolute path
     *
     * @see Class#getResource(java.lang.String)
     * @see File
     */
    File testFile(String relativePath) {
        def resourceName = "/test-data/java/${relativePath}"
        def resource = this.class.getResource(resourceName)
        assert resource: "${resourceName} not found on classpath"

        def file = new File(resource.path)
        assert file: "Could not find file for ${resourceName}"
        assert file.exists(): "${file} does not exist"

        return file
    }

    // -- helper methods -----------------------------------------------------------------------------------------------

    def getTasks() {
        return project.tasks
    }

    def cpd(Closure closure) {
        return project.extensions.getByName('cpd')
    }
}
