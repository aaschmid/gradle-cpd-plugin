package de.aaschmid.gradle.plugins.cpd.test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.Project;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFileResolver {

    public enum Lang {
        JAVA("java"),
        KOTLIN("kotlin");

        private final String folder;

        Lang(String folder) {
            this.folder = folder;
        }
    }

    /**
     * Creates a {@link File} with location <code>classpath:/test-data/java/${relativePath}</code> as absolute path
     *
     * @see Class#getResource(java.lang.String)
     * @see File
     */
    public static File testFile(Lang lang, String relativePath) {
        String resourceName = String.format("/test-data/%s/%s", lang.folder, relativePath);
        URL resource = TestFileResolver.class.getResource(resourceName);
        assertThat(resource).as("%s not found on classpath.", resourceName).isNotNull();

        File file = new File(resource.getPath());
        assertThat(file).as("Could not find file for %s.", resourceName).isNotNull();
        assertThat(file.exists()).as("%s does not exist.", file).isTrue();
        return file;
    }

    public static List<File> testFilesRecurseIn(Lang lang, String... relativePaths) {
        return Arrays.stream(relativePaths).flatMap(relativePath -> {
            Path filePath = testFile(lang, relativePath).toPath();
            if (Files.isDirectory(filePath)) {
                try {
                    return Files.find(filePath, Integer.MAX_VALUE, (path, fileAttr) -> fileAttr.isRegularFile()).map(Path::toFile);
                } catch (IOException e) {
                    throw new UncheckedIOException(String.format("Exception while recursively search for files in %s", relativePath), e);
                }
            }
            return Stream.empty();
        }).collect(Collectors.toList());
    }

    public static List<File> createProjectFiles(Project project, String... files) {
        return Arrays.stream(files).map(filePath -> {
            File file = project.file(filePath);
            try {
                if (file.isDirectory()) {
                    Files.createDirectories(file.toPath());
                } else {
                    Files.createDirectories(file.toPath().getParent());
                    Files.createFile(file.toPath());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(String.format("Exception while creating file %s", file), e);
            }
            return file;
        }).collect(Collectors.toList());
    }
}
