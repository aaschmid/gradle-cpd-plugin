package de.aaschmid.gradle.plugins.cpd.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TestFileResolver {


    /**
     * Creates a {@link File} with location <code>classpath:/test-data/java/${relativePath}</code> as absolute path
     *
     * @see Class#getResource(java.lang.String)
     * @see File
     */
    public static File testFile(String relativePath) {
        return testFile("java", relativePath);
    }

    public static File testFile(String lang, String relativePath) {
        String resourceName = String.format("/test-data/%s/%s", lang, relativePath);
        URL resource = TestFileResolver.class.getResource(resourceName);
        assertThat(resource).as("%s not found on classpath.", resourceName).isNotNull();

        File file = new File(resource.getPath());
        assertThat(file).as("Could not find file for %s.", resourceName).isNotNull();
        assertThat(file.exists()).as("%s does not exist.", file).isTrue();
        return file;
    }

    public static List<File> testFilesRecurseIn(String relativePath) {
        try {
            return Files
                    .find(testFile(relativePath).toPath(), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UndeclaredThrowableException(e, String.format("Exception while recursively search for files in %s", relativePath));
        }
    }
}
