/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.executer;

import org.apache.commons.collections.CollectionUtils;
import org.gradle.api.Action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class OutputScrapingExecutionResult {
    private final String output;
    private final String error;

    //for example: ':a SKIPPED' or ':foo:bar:baz UP-TO-DATE' but not ':a'
    private final Pattern skippedTaskPattern = Pattern.compile("(:\\S+?(:\\S+?)*)\\s+((SKIPPED)|(UP-TO-DATE))");

    //for example: ':hey' or ':a SKIPPED' or ':foo:bar:baz UP-TO-DATE' but not ':a FOO'
    private final Pattern taskPattern = Pattern.compile("(:\\S+?(:\\S+?)*)((\\s+SKIPPED)|(\\s+UP-TO-DATE)|(\\s+FAILED)|(\\s*))");

    public OutputScrapingExecutionResult(String output, String error) {
        this.output = output;
        this.error = error;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }

    /**
     * Returns the tasks have been executed in order (includes tasks that were skipped). Note: ignores buildSrc tasks.
     */
    public List<String> getExecutedTasks() {
        return grepTasks(taskPattern);
    }

    /**
     * Asserts that exactly the given set of tasks have been executed in the given order. Note: ignores buildSrc tasks.
     */
    public OutputScrapingExecutionResult assertTasksExecuted(String... taskPaths) {
        List<String> expectedTasks = Arrays.asList(taskPaths);
        assertThat(String.format("Expected tasks %s not found in process output:%n%s", expectedTasks, getOutput()), getExecutedTasks(), equalTo(expectedTasks));
        return this;
    }

    public OutputScrapingExecutionResult assertTaskNotExecuted(String taskPath) {
        Set<String> tasks = new HashSet<String>(getExecutedTasks());
        assertThat(String.format("Expected task %s found in process output:%n%s", taskPath, getOutput()), tasks, not(hasItem(taskPath)));
        return this;
    }

    /**
     * Returns the tasks that were skipped, in an undefined order. Note: ignores buildSrc tasks.
     */
    public Set<String> getSkippedTasks() {
        return new HashSet<String>(grepTasks(skippedTaskPattern));
    }

    /**
     * Asserts that exactly the given set of tasks have been skipped. Note: ignores buildSrc tasks.
     */
    public OutputScrapingExecutionResult assertTasksSkipped(String... taskPaths) {
        Set<String> expectedTasks = new HashSet<String>(Arrays.asList(taskPaths));
        assertThat(String.format("Expected skipped tasks %s not found in process output:%n%s", expectedTasks, getOutput()), getSkippedTasks(), equalTo(expectedTasks));
        return this;
    }

    /**
     * Asserts the given task has been skipped. Note: ignores buildSrc tasks.
     */
    public OutputScrapingExecutionResult assertTaskSkipped(String taskPath) {
        Set<String> tasks = new HashSet<String>(getSkippedTasks());
        assertThat(String.format("Expected skipped task %s not found in process output:%n%s", taskPath, getOutput()), tasks, hasItem(taskPath));
        return this;
    }

    /**
     * Asserts that exactly the given set of tasks have not been skipped. Note: ignores buildSrc tasks.
     */
    public OutputScrapingExecutionResult assertTasksNotSkipped(String... taskPaths) {
        Set<String> tasks = new HashSet<String>(getNotSkippedTasks());
        Set<String> expectedTasks = new HashSet<String>(Arrays.asList(taskPaths));
        assertThat(String.format("Expected executed tasks %s not found in process output:%n%s", expectedTasks, getOutput()), tasks, equalTo(expectedTasks));
        return this;
    }

    private Collection<String> getNotSkippedTasks() {
        List all = getExecutedTasks();
        Set skipped = getSkippedTasks();
        return CollectionUtils.subtract(all, skipped);
    }

    /**
     * Asserts that the given task has not been skipped. Note: ignores buildSrc tasks.
     */
    public OutputScrapingExecutionResult assertTaskNotSkipped(String taskPath) {
        Set<String> tasks = new HashSet<String>(getNotSkippedTasks());
        assertThat(String.format("Expected executed task %s not found in process output:%n%s", taskPath, getOutput()), tasks, hasItem(taskPath));
        return this;
    }

    private List<String> grepTasks(final Pattern pattern) {
        final LinkedList<String> tasks = new LinkedList<String>();

        eachLine(new Action<String>() {
            public void execute(String s) {
                java.util.regex.Matcher matcher = pattern.matcher(s);
                if (matcher.matches()) {
                    String taskName = matcher.group(1);
                    if (!taskName.startsWith(":buildSrc:")) {
                        //for INFO/DEBUG level the task may appear twice - once for the execution, once for the UP-TO-DATE
                        //so I'm not adding the task to the list if it is the same as previously added task.
                        if (tasks.size() == 0 || !tasks.getLast().equals(taskName)) {
                            tasks.add(taskName);
                        }
                    }
                }
            }
        });

        return tasks;
    }

    private void eachLine(Action<String> action) {
        BufferedReader reader = new BufferedReader(new StringReader(getOutput()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                action.execute(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
