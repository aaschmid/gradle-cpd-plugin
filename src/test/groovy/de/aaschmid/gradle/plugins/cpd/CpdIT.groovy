package de.aaschmid.gradle.plugins.cpd


import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CpdIT extends Specification {

    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "executing 'Cpd' task on non-duplicate 'java' source will produce empty 'cpdCheck.xml'"() {
        given:
        buildFile << """
        cpd {
            encoding = 'ISO-8859-1'
            minimumTokenCount = 10
        }
        """
        fillJavaDir('de/aaschmid/foo')


        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('cpdCheck')
                .build()

        then:
        result.task(":cpdCheck").outcome == SUCCESS
        def report = project.file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        // TODO do better?
        report.text =~ /encoding="ISO-8859-1"/
        report.text =~ /<pmd-cpd\/>/
    }

    private void fillJavaDir(String path) {
        def dir = new File(testProjectDir.root, "src/main/java/$path");
        dir.mkdirs();

        ProjectBuilder.builder().build().copy {
            from testFile(path)
            into dir
        }
    }

    /**
     * Creates a {@link File} with location <code>classpath:/test-data/java/${relativePath}</code> as absolute path
     *
     * @see Class#getResource(java.lang.String)
     * @see File
     */
    private File testFile(String relativePath) {
        def resourceName = "/test-data/java/${relativePath}"
        def resource = this.class.getResource(resourceName)
        assert resource: "${resourceName} not found on classpath"

        def file = new File(resource.path)
        assert file: "Could not find file for ${resourceName}"
        assert file.exists(): "${file} does not exist"

        return file
    }
}
