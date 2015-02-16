/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.executer;

import org.gradle.util.TextUtil;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static de.aaschmid.gradle.plugins.cpd.test.fixtures.util.Matchers.isEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class OutputScrapingExecutionFailure extends OutputScrapingExecutionResult {
    private static final Pattern FAILURE_PATTERN = Pattern.compile("(?m)FAILURE: .+$");
    private static final Pattern CAUSE_PATTERN = Pattern.compile("(?m)(^\\s*> )");
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("(?ms)^\\* What went wrong:$(.+?)^\\* Try:$");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(?ms)^\\* Where:((.+)'.+') line: (\\d+)$");
    private static final Pattern RESOLUTION_PATTERN = Pattern.compile("(?ms)^\\* Try:$(.+?)^\\* Exception is:$");
    private final String description;
    private final String lineNumber;
    private final String fileName;
    private final String resolution;
    private final List<String> causes = new ArrayList<String>();

    public OutputScrapingExecutionFailure(String output, String error) {
        super(output, error);

        java.util.regex.Matcher matcher = FAILURE_PATTERN.matcher(error);
        if (matcher.find()) {
            if (matcher.find()) {
                throw new AssertionError("Found multiple failure sections in build error output.");
            }
        }

        matcher = LOCATION_PATTERN.matcher(error);
        if (matcher.find()) {
            fileName = matcher.group(1).trim();
            lineNumber = matcher.group(3);
        } else {
            fileName = "";
            lineNumber = "";
        }

        matcher = DESCRIPTION_PATTERN.matcher(error);
        if (matcher.find()) {
            String problemStr = matcher.group(1);
            Problem problem = extract(problemStr);
            description = problem.description;
            causes.addAll(problem.causes);
            while (matcher.find()) {
                problemStr = matcher.group(1);
                problem = extract(problemStr);
                causes.addAll(problem.causes);
            }
        } else {
            description = "";
        }

        matcher = RESOLUTION_PATTERN.matcher(error);
        if (!matcher.find()) {
            resolution = "";
        } else {
            resolution = matcher.group(1).trim();
        }
    }

    private Problem extract(String problem) {
        java.util.regex.Matcher matcher = CAUSE_PATTERN.matcher(problem);
        String description;
        List<String> causes = new ArrayList<String>();
        if (!matcher.find()) {
            description = TextUtil.normaliseLineSeparators(problem.trim());
        } else {
            description = TextUtil.normaliseLineSeparators(problem.substring(0, matcher.start()).trim());
            while (true) {
                int pos = matcher.end();
                int prefix = matcher.group(1).length();
                String prefixPattern = toPrefixPattern(prefix);
                if (matcher.find(pos)) {
                    String cause = TextUtil.normaliseLineSeparators(problem.substring(pos, matcher.start()).trim().replaceAll(prefixPattern, ""));
                    causes.add(cause);
                } else {
                    String cause = TextUtil.normaliseLineSeparators(problem.substring(pos).trim().replaceAll(prefixPattern, ""));
                    causes.add(cause);
                    break;
                }
            }
        }
        return new Problem(description, causes);
    }

    private String toPrefixPattern(int prefix) {
        StringBuilder builder = new StringBuilder("(?m)^");
        for (int i = 0; i < prefix; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    public OutputScrapingExecutionFailure assertHasLineNumber(int lineNumber) {
        assertThat(this.lineNumber, equalTo(String.valueOf(lineNumber)));
        return this;
    }

    public OutputScrapingExecutionFailure assertHasFileName(String filename) {
        assertThat(this.fileName, equalTo(filename));
        return this;
    }

    /**
     * Asserts that the reported failure has the given cause (ie the bit after the description)
     */
    public OutputScrapingExecutionFailure assertHasCause(String description) {
        assertThatCause(startsWith(description));
        return this;
    }

    /**
     * Asserts that the reported failure has the given cause (ie the bit after the description)
     */
    public OutputScrapingExecutionFailure assertThatCause(Matcher<String> matcher) {
        for (String cause : causes) {
            if (matcher.matches(cause)) {
                return this;
            }
        }
        fail(String.format("No matching cause found in %s", causes));
        return this;
    }

    /**
     * Asserts that the reported failure has the given resolution (ie the bit after '* Try').
     */
    public OutputScrapingExecutionFailure assertHasResolution(String resolution) {
        assertThat(this.resolution, equalTo(resolution));
        return this;
    }

    public OutputScrapingExecutionFailure assertHasNoCause() {
        assertThat(causes, isEmpty());
        return this;
    }

    /**
     * Asserts that the reported failure has the given description (ie the bit after '* What went wrong').
     */
    public OutputScrapingExecutionFailure assertHasDescription(String context) {
        assertThatDescription(equalTo(context));
        return this;
    }

    /**
     * Asserts that the reported failure has the given description (ie the bit after '* What went wrong').
     */
    public OutputScrapingExecutionFailure assertThatDescription(Matcher<String> matcher) {
        assertThat(description, matcher);
        return this;
    }

    public OutputScrapingExecutionFailure assertTestsFailed() {
        return this.assertHasDescription("Execution failed for task ':test'.").assertThatCause(startsWith("There were failing tests"));
    }

    private static class Problem {
        final String description;
        final List<String> causes;

        private Problem(String description, List<String> causes) {
            this.description = description;
            this.causes = causes;
        }
    }
}