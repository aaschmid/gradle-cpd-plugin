package de.aaschmid.gradle.plugins.cpd.test

import groovy.io.FileType
import spock.lang.Specification

enum Lang {
    JAVA("java"),
    KOTLIN("kotlin");

    private final String folder

    Lang(String folder) {
        this.folder = folder;
    }
}

abstract class BaseSpec extends Specification {

    /**
     * Creates a {@link File} with location <code>classpath:/test-data/java/${relativePath}</code> as absolute path
     *
     * @see Class#getResource(java.lang.String)
     * @see File
     */
    File testFile(String relativePath) {
        return testFile(Lang.JAVA, relativePath)
    }

    File testFile(Lang lang, String relativePath) {
        def resourceName = "/test-data/${lang.folder}/${relativePath}"
        def resource = this.class.getResource(resourceName)
        assert resource: "${resourceName} not found on classpath"

        def file = new File(resource.path)
        assert file: "Could not find file for ${resourceName}"
        assert file.exists(): "${file} does not exist"

        return file
    }

    List<File> testFilesRecurseIn(String relativePath) {
        def result = [ ]
        testFile(relativePath).eachFileRecurse(FileType.FILES){ file -> result << file }
        return result;
    }

    String testPath(Lang lang, String...relativePaths) {
        return "['${relativePaths.collect{testFile(lang, it).getAbsolutePath()}.join("', '")}']"
    }
}
