/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.executer;

import de.aaschmid.gradle.plugins.cpd.test.fixtures.file.TestDirectoryProvider;
import de.aaschmid.gradle.plugins.cpd.test.fixtures.file.TestFile;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.internal.ClosureBackedAction;
import org.gradle.api.internal.initialization.DefaultClassLoaderScope;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.jvm.Jvm;
import org.gradle.listener.ActionBroadcast;
import org.gradle.process.internal.JvmOptions;
import org.gradle.util.DeprecationLogger;
import org.gradle.util.TextUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static de.aaschmid.gradle.plugins.cpd.test.fixtures.util.Matchers.containsLine;
import static de.aaschmid.gradle.plugins.cpd.test.fixtures.util.Matchers.matchesRegexp;
import static java.util.Arrays.asList;


public abstract class GradleExecuter {

    private final Logger logger;

    private final List<String> args = new ArrayList<String>();
    private final List<String> tasks = new ArrayList<String>();
    private boolean allowExtraLogging = true;
    private File workingDir;
    private boolean quiet;
    private boolean taskList;
    private boolean dependencyList;
    private boolean searchUpwards;
    private Map<String, String> environmentVars = new HashMap<String, String>();
    private List<File> initScripts = new ArrayList<File>();
    private String executable;
    private File userHomeDir;
    private File javaHome;
    private File buildScript;
    private File projectDir;
    private File settingsFile;
    private InputStream stdin;
    private String defaultCharacterEncoding;
    private Locale defaultLocale;
    private final List<String> gradleOpts = new ArrayList<String>();
    private boolean noDefaultJvmArgs;
    private boolean requireGradleHome;

    private boolean deprecationChecksOn = true;
    private boolean eagerClassLoaderCreationChecksOn = true;
    private boolean stackTraceChecksOn = true;

    private final ActionBroadcast<GradleExecuter> beforeExecute = new ActionBroadcast<GradleExecuter>();
    private final Set<Action<? super GradleExecuter>> afterExecute = new LinkedHashSet<Action<? super GradleExecuter>>();

    private final IntegrationTestBuildContext buildContext;
    private final TestDirectoryProvider testDirectoryProvider;

    protected GradleExecuter(IntegrationTestBuildContext buildContext, TestDirectoryProvider testDirectoryProvider) {
        this.buildContext = buildContext;
        this.testDirectoryProvider = testDirectoryProvider;
        logger = Logging.getLogger(getClass());
    }

    protected Logger getLogger() {
        return logger;
    }

    public GradleExecuter reset() {
        args.clear();
        tasks.clear();
        initScripts.clear();
        workingDir = null;
        projectDir = null;
        buildScript = null;
        settingsFile = null;
        quiet = false;
        taskList = false;
        dependencyList = false;
        searchUpwards = false;
        executable = null;
        javaHome = null;
        environmentVars.clear();
        stdin = null;
        defaultCharacterEncoding = null;
        defaultLocale = null;
        noDefaultJvmArgs = false;
        deprecationChecksOn = true;
        stackTraceChecksOn = true;
        return this;
    }

    /**
     * The buildContext used to execute.
     */
    public IntegrationTestBuildContext getBuildContext() {
        return buildContext;
    }

    /**
     * The directory that the executer will use for any test specific storage.
     * <p/>
     * May or may not be the same directory as the build to be run.
     */
    public TestDirectoryProvider getTestDirectoryProvider() {
        return testDirectoryProvider;
    }

    /**
     * Adds an action to be called immediately before execution, to allow extra configuration to be injected.
     */
    public void beforeExecute(Action<? super GradleExecuter> action) {
        beforeExecute.add(action);
    }

    /**
     * Adds an action to be called immediately before execution, to allow extra configuration to be injected.
     */
    public void beforeExecute(@DelegatesTo(GradleExecuter.class) Closure action) {
        beforeExecute.add(new ClosureBackedAction<GradleExecuter>(action));
    }

    /**
     * Adds an action to be called immediately after execution
     */
    public void afterExecute(Action<? super GradleExecuter> action) {
        afterExecute.add(action);
    }

    /**
     * Adds an action to be called immediately after execution
     */
    public void afterExecute(@DelegatesTo(GradleExecuter.class) Closure action) {
        afterExecute.add(new ClosureBackedAction<GradleExecuter>(action));
    }

    /**
     * Sets the working directory to use. Defaults to the test's temporary directory.
     */
    public GradleExecuter inDirectory(File directory) {
        workingDir = directory;
        return this;
    }

    public File getWorkingDir() {
        return workingDir == null ? getTestDirectoryProvider().getTestDirectory() : workingDir;
    }

    /**
     * Uses the given build script
     */
    public GradleExecuter usingBuildScript(File buildScript) {
        this.buildScript = buildScript;
        return this;
    }

    /**
     * Uses the given project directory
     */
    public GradleExecuter usingProjectDirectory(File projectDir) {
        this.projectDir = projectDir;
        return this;
    }

    public GradleExecuter usingSettingsFile(File settingsFile) {
        this.settingsFile = settingsFile;
        return this;
    }

    public GradleExecuter usingInitScript(File initScript) {
        initScripts.add(initScript);
        return this;
    }

    /**
     * The Gradle user home dir that will be used for executions.
     */
    public TestFile getWorkerDir() {
        return buildContext.getIntegTestWorkerDir();
    }

    public File getUserHomeDir() {
        return userHomeDir;
    }

    /**
     * Returns the gradle opts set with withGradleOpts() (does not consider any set via withEnvironmentVars())
     */
    protected List<String> getGradleOpts() {
        return gradleOpts;
    }

    /**
     * Sets the user's home dir to use when running the build. Implementations are not 100% accurate.
     */
    public GradleExecuter withUserHomeDir(File userHomeDir) {
        this.userHomeDir = userHomeDir;
        return this;
    }

    public File getJavaHome() {
        return javaHome == null ? Jvm.current().getJavaHome() : javaHome;
    }

    /**
     * Sets the java home dir. Setting to null requests that the executer use the real default java home dir rather than
     * the default used for testing.
     */
    public GradleExecuter withJavaHome(File javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    /**
     * Sets the executable to use. Set to null to use the read default executable (if any) rather than the default used
     * for testing.
     */
    public GradleExecuter usingExecutable(String script) {
        this.executable = script;
        return this;
    }

    public String getExecutable() {
        return executable;
    }

    /**
     * Sets the stdin to use for the build. Defaults to an empty string.
     */
    public GradleExecuter withStdIn(String text) {
        this.stdin = new ByteArrayInputStream(TextUtil.toPlatformLineSeparators(text).getBytes());
        return this;
    }

    /**
     * Sets the stdin to use for the build. Defaults to an empty string.
     */
    public GradleExecuter withStdIn(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    public InputStream getStdin() {
        return stdin == null ? new ByteArrayInputStream(new byte[0]) : stdin;
    }

    /**
     * Sets the default character encoding to use.
     * <p/>
     * Only makes sense for forking executers.
     *
     * @return this executer
     */
    public GradleExecuter withDefaultCharacterEncoding(String defaultCharacterEncoding) {
        this.defaultCharacterEncoding = defaultCharacterEncoding;
        return this;
    }

    public String getDefaultCharacterEncoding() {
        return defaultCharacterEncoding == null ? Charset.defaultCharset().name() : defaultCharacterEncoding;
    }

    /**
     * Sets the default locale to use.
     * <p/>
     * Only makes sense for forking executers.
     *
     * @return this executer
     */
    public GradleExecuter withDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        return this;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Enables search upwards. Defaults to false.
     */
    public GradleExecuter withSearchUpwards() {
        searchUpwards = true;
        return this;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public GradleExecuter withQuietLogging() {
        quiet = true;
        return this;
    }

    public GradleExecuter withTaskList() {
        taskList = true;
        return this;
    }

    public GradleExecuter withDependencyList() {
        dependencyList = true;
        return this;
    }

    /**
     * Sets the additional command-line arguments to use when executing the build. Defaults to an empty list.
     */
    public GradleExecuter withArguments(String... args) {
        return withArguments(Arrays.asList(args));
    }

    /**
     * Sets the additional command-line arguments to use when executing the build. Defaults to an empty list.
     */
    public GradleExecuter withArguments(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
        return this;
    }

    /**
     * Adds an additional command-line argument to use when executing the build.
     */
    public GradleExecuter withArgument(String arg) {
        this.args.add(arg);
        return this;
    }

    /**
     * Sets the environment variables to use when executing the build. Defaults to the environment of this process.
     */
    public GradleExecuter withEnvironmentVars(Map<String, ?> environment) {
        environmentVars.clear();
        for (Map.Entry<String, ?> entry : environment.entrySet()) {
            environmentVars.put(entry.getKey(), entry.getValue().toString());
        }
        return this;
    }

    /**
     * Returns the effective env vars, having merged in specific settings.
     * <p/>
     * For example, GRADLE_OPTS will be anything that was specified via withEnvironmentVars() and withGradleOpts().
     * JAVA_HOME will also be set according to getJavaHome().
     */
    protected Map<String, String> getMergedEnvironmentVars() {
        Map<String, String> environmentVars = new HashMap<String, String>(getEnvironmentVars());
        environmentVars.put("GRADLE_OPTS", toJvmArgsString(getMergedGradleOpts()));
        if (!environmentVars.containsKey("JAVA_HOME")) {
            environmentVars.put("JAVA_HOME", getJavaHome().getAbsolutePath());
        }
        return environmentVars;
    }

    protected String toJvmArgsString(Iterable<String> jvmArgs) {
        StringBuilder result = new StringBuilder();
        for (String jvmArg : jvmArgs) {
            if (result.length() > 0) {
                result.append(" ");
            }
            if (jvmArg.contains(" ")) {
                assert !jvmArg.contains("\"") : "jvmArg '" + jvmArg + "' contains '\"'";
                result.append('"');
                result.append(jvmArg);
                result.append('"');
            } else {
                result.append(jvmArg);
            }
        }

        return result.toString();
    }

    private List<String> getMergedGradleOpts() {
        List<String> gradleOpts = new ArrayList<String>(getGradleOpts());
        String gradleOptsEnv = getEnvironmentVars().get("GRADLE_OPTS");
        if (gradleOptsEnv != null) {
            gradleOpts.addAll(JvmOptions.fromString(gradleOptsEnv));
        }

        return gradleOpts;
    }

    protected Map<String, String> getEnvironmentVars() {
        return environmentVars;
    }

    /**
     * Sets the task names to execute. Defaults to an empty list.
     */
    public GradleExecuter withTasks(String... names) {
        return withTasks(Arrays.asList(names));
    }

    /**
     * Sets the task names to execute. Defaults to an empty list.
     */
    public GradleExecuter withTasks(List<String> names) {
        tasks.clear();
        tasks.addAll(names);
        return this;
    }

    /**
     * Specifies that the executer should not set any default jvm args.
     */
    public GradleExecuter withNoDefaultJvmArgs() {
        noDefaultJvmArgs = true;
        return this;
    }

    protected boolean isNoDefaultJvmArgs() {
        return noDefaultJvmArgs;
    }

    protected List<String> getAllArgs() {
        List<String> allArgs = new ArrayList<String>();
        if (buildScript != null) {
            allArgs.add("--build-file");
            allArgs.add(buildScript.getAbsolutePath());
        }
        if (projectDir != null) {
            allArgs.add("--project-dir");
            allArgs.add(projectDir.getAbsolutePath());
        }
        for (File initScript : initScripts) {
            allArgs.add("--init-script");
            allArgs.add(initScript.getAbsolutePath());
        }
        if (settingsFile != null) {
            allArgs.add("--settings-file");
            allArgs.add(settingsFile.getAbsolutePath());
        }
        if (quiet) {
            allArgs.add("--quiet");
        }
        if (taskList) {
            allArgs.add("tasks");
        }
        if (dependencyList) {
            allArgs.add("dependencies");
        }

        if (!searchUpwards) {
            boolean settingsFoundAboveInTestDir = false;
            TestFile dir = new TestFile(getWorkingDir());
            while (dir != null && getTestDirectoryProvider().getTestDirectory().isSelfOrDescendent(dir)) {
                if (dir.file("settings.gradle").isFile()) {
                    settingsFoundAboveInTestDir = true;
                    break;
                }
                dir = dir.getParentFile();
            }

            if (!settingsFoundAboveInTestDir) {
                allArgs.add("--no-search-upward");
            }
        }

        // This will cause problems on Windows if the path to the Gradle executable that is used has a space in it (e.g. the user's dir is c:/Users/Luke Daley/)
        // This is fundamentally a windows issue: You can't have arguments with spaces in them if the path to the batch script has a space
        // We could work around this by setting -Dgradle.user.home but GRADLE-1730 (which affects 1.0-milestone-3) means that that
        // is problematic as well. For now, we just don't support running the int tests from a path with a space in it on Windows.
        // When we stop testing against M3 we should change to use the system property.
        if (getWorkerDir() != null) {
            allArgs.add("--gradle-user-home");
            allArgs.add(getWorkerDir().getAbsolutePath());
        }

        allArgs.addAll(args);
        allArgs.addAll(tasks);
        return allArgs;
    }

    protected Map<String, String> getImplicitJvmSystemProperties() {
        Map<String, String> properties = new LinkedHashMap<String, String>();

        if (getUserHomeDir() != null) {
            properties.put("user.home", getUserHomeDir().getAbsolutePath());
        }

        properties.put(DeprecationLogger.ORG_GRADLE_DEPRECATION_TRACE_PROPERTY_NAME, "true");

        properties.put("java.io.tmpdir", getTmpDir().createDir().getAbsolutePath());

        properties.put("file.encoding", getDefaultCharacterEncoding());
        Locale locale = getDefaultLocale();
        if (locale != null) {
            properties.put("user.language", locale.getLanguage());
            properties.put("user.country", locale.getCountry());
            properties.put("user.variant", locale.getVariant());
        }

        if (eagerClassLoaderCreationChecksOn) {
            properties.put(DefaultClassLoaderScope.STRICT_MODE_PROPERTY, "true");
        }

        return properties;
    }

    /**
     * Starts executing the build asynchronously.
     *
     * @return the handle, never null.
     */
    public final GradleHandle start() {
        assert afterExecute.isEmpty() : "afterExecute actions are not implemented for async execution";
        fireBeforeExecute();
        assertCanExecute();
        try {
            return doStart();
        } finally {
            reset();
        }
    }

    /**
     * Executes the requested build, asserting that the build succeeds. Resets the configuration of this executer.
     *
     * @return The result.
     */
    public final OutputScrapingExecutionResult run() {
        fireBeforeExecute();
        assertCanExecute();
        try {
            return doRun();
        } finally {
            finished();
        }
    }

    private void finished() {
        try {
            new ActionBroadcast<GradleExecuter>(afterExecute).execute(this);
        } finally {
            reset();
        }
    }

    /**
     * Executes the requested build, asserting that the build fails. Resets the configuration of this executer.
     *
     * @return The result.
     */
    public final OutputScrapingExecutionFailure runWithFailure() {
        fireBeforeExecute();
        assertCanExecute();
        try {
            return doRunWithFailure();
        } finally {
            finished();
        }
    }

    private void fireBeforeExecute() {
        beforeExecute.execute(this);
    }

    protected GradleHandle doStart() {
        throw new UnsupportedOperationException(String.format("%s does not support running asynchronously.", getClass().getSimpleName()));
    }

    protected abstract OutputScrapingExecutionResult doRun();

    protected abstract OutputScrapingExecutionFailure doRunWithFailure();


    /**
     * Asserts that this executer will be able to run a build, given its current configuration.
     *
     * @throws AssertionError When this executer will not be able to run a build.
     */
    protected abstract void assertCanExecute() throws AssertionError;

    /**
     * Adds options that should be used to start the JVM, if a JVM is to be started. Ignored if not.
     *
     * @param gradleOpts the jvm opts
     * @return this executer
     */
    public GradleExecuter withGradleOpts(String... gradleOpts) {
        this.gradleOpts.addAll(asList(gradleOpts));
        return this;
    }

    protected Action<OutputScrapingExecutionResult> getResultAssertion() {
        ActionBroadcast<OutputScrapingExecutionResult> assertions = new ActionBroadcast<OutputScrapingExecutionResult>();

        if (stackTraceChecksOn) {
            assertions.add(new Action<OutputScrapingExecutionResult>() {
                public void execute(OutputScrapingExecutionResult executionResult) {
                    assertNoStackTraces(executionResult.getOutput(), "Standard output");

                    String error = executionResult.getError();
                    if (executionResult instanceof OutputScrapingExecutionFailure) {
                        // Axe everything after the expected exception
                        int pos = error.indexOf("* Exception is:" + TextUtil.getPlatformLineSeparator());
                        if (pos >= 0) {
                            error = error.substring(0, pos);
                        }
                    }
                    assertNoStackTraces(error, "Standard error");
                }

                private void assertNoStackTraces(String output, String displayName) {
                    if (containsLine(matchesRegexp("\\s+(at\\s+)?[\\w.$_]+\\([\\w._]+:\\d+\\)")).matches(output)) {
                        throw new AssertionError(String.format("%s contains an unexpected stack trace:%n=====%n%s%n=====%n", displayName, output));
                    }
                }
            });
        }

        if (deprecationChecksOn) {
            assertions.add(new Action<OutputScrapingExecutionResult>() {
                public void execute(OutputScrapingExecutionResult executionResult) {
                    assertNoDeprecationWarnings(executionResult.getOutput(), "Standard output");
                    assertNoDeprecationWarnings(executionResult.getError(), "Standard error");
                }

                private void assertNoDeprecationWarnings(String output, String displayName) {
                    boolean javacWarning = containsLine(matchesRegexp(".*use(s)? or override(s)? a deprecated API\\.")).matches(output);
                    boolean deprecationWarning = containsLine(matchesRegexp(".* deprecated.*")).matches(output);
                    if (deprecationWarning && !javacWarning) {
                        throw new AssertionError(String.format("%s contains a deprecation warning:%n=====%n%s%n=====%n", displayName, output));
                    }
                }
            });
        }

        return assertions;
    }

    /**
     * Disables asserting that the execution did not trigger any deprecation warnings.
     */
    public GradleExecuter withDeprecationChecksDisabled() {
        deprecationChecksOn = false;
        // turn off stack traces too
        stackTraceChecksOn = false;
        return this;
    }

    /**
     * Disables asserting that class loaders were not eagerly created, potentially leading to performance problems.
     */
    public GradleExecuter withEagerClassLoaderCreationCheckDisabled() {
        eagerClassLoaderCreationChecksOn = false;
        return this;
    }

    /**
     * Disables asserting that no unexpected stacktraces are present in the output.
     */
    public GradleExecuter withStackTraceChecksDisabled() {
        stackTraceChecksOn = false;
        return this;
    }

    protected TestFile getTmpDir() {
        return new TestFile(getTestDirectoryProvider().getTestDirectory(), "tmp");
    }

    /**
     * An executer may decide to implicitly bump the logging level, unless this is called.
     */
    public GradleExecuter noExtraLogging() {
        this.allowExtraLogging = false;
        return this;
    }

    public boolean isAllowExtraLogging() {
        return allowExtraLogging;
    }

    public boolean isRequireGradleHome() {
        return requireGradleHome;
    }

    /**
     * Requires that there is a gradle home for the execution, which in process execution does not.
     */
    public GradleExecuter requireGradleHome() {
        this.requireGradleHome = true;
        return this;
    }
}
