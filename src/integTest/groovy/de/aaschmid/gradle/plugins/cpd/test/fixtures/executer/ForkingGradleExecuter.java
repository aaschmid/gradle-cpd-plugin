/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.executer;

import de.aaschmid.gradle.plugins.cpd.test.fixtures.file.TestDirectoryProvider;
import de.aaschmid.gradle.plugins.cpd.test.fixtures.file.TestFile;
import org.gradle.api.Action;
import org.gradle.internal.Factory;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.internal.ExecHandleBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

class ForkingGradleExecuter extends GradleExecuter {

    public ForkingGradleExecuter(IntegrationTestBuildContext buildContext, TestDirectoryProvider testDirectoryProvider) {
        super(buildContext, testDirectoryProvider);
    }

    public void assertCanExecute() throws AssertionError {
        // nothing to do
    }

    @Override
    protected List<String> getAllArgs() {
        List<String> args = new ArrayList<String>();
        args.addAll(super.getAllArgs());
        args.add("--stacktrace");
        return args;
    }

    private ExecHandleBuilder createExecHandleBuilder() {
        TestFile gradleHomeDir = getBuildContext().getGradleHomeDir();
        if (!gradleHomeDir.isDirectory()) {
            fail(gradleHomeDir + " is not a directory.\n"
                    + "If you are running tests from IDE make sure that gradle tasks that prepare the test image were executed. Last time it was 'intTestImage' task.");
        }

        ExecHandleBuilder builder = new ExecHandleBuilder() {
            @Override
            public File getWorkingDir() {
                // Override this, so that the working directory is not canonicalised. Some int tests require that
                // the working directory is not canonicalised
                return ForkingGradleExecuter.this.getWorkingDir();
            }
        };

        // Clear the user's environment
        builder.environment("GRADLE_HOME", "");
        builder.environment("JAVA_HOME", "");
        builder.environment("GRADLE_OPTS", "");
        builder.environment("JAVA_OPTS", "");

        builder.environment(getMergedEnvironmentVars());
        builder.workingDir(getWorkingDir());
        builder.setStandardInput(getStdin());

        builder.args(getAllArgs());

        ExecHandlerConfigurer configurer = OperatingSystem.current().isWindows() ? new WindowsConfigurer() : new UnixConfigurer();
        configurer.configure(builder);

        getLogger().info(String.format("Execute in %s with: %s %s", builder.getWorkingDir(), builder.getExecutable(), builder.getArgs()));

        return builder;
    }

    @Override
    public GradleHandle doStart() {
        return createGradleHandle(getResultAssertion(), getDefaultCharacterEncoding(), new Factory<ExecHandleBuilder>() {
            public ExecHandleBuilder create() {
                return createExecHandleBuilder();
            }
        }).start();
    }

    protected GradleHandle createGradleHandle(Action<OutputScrapingExecutionResult> resultAssertion, String encoding, Factory<ExecHandleBuilder> execHandleFactory) {
        return new GradleHandle(resultAssertion, encoding, execHandleFactory);
    }

    protected OutputScrapingExecutionResult doRun() {
        return start().waitForFinish();
    }

    protected OutputScrapingExecutionFailure doRunWithFailure() {
        return start().waitForFailure();
    }

    @Override
    protected List<String> getGradleOpts() {
        List<String> gradleOpts = new ArrayList<java.lang.String>(super.getGradleOpts());
        for (Map.Entry<String, String> entry : getImplicitJvmSystemProperties().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            gradleOpts.add(String.format("-D%s=%s", key, value));
        }
        gradleOpts.add("-ea");

        // uncomment for debugging
//        gradleOpts.add("-Xdebug");
//        gradleOpts.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");

        return gradleOpts;
    }

    private interface ExecHandlerConfigurer {
        void configure(ExecHandleBuilder builder);
    }

    private class WindowsConfigurer implements ExecHandlerConfigurer {
        public void configure(ExecHandleBuilder builder) {
            String cmd;
            if (getExecutable() != null) {
                cmd = getExecutable().replace('/', File.separatorChar);
            } else {
                cmd = "gradle";
            }
            builder.executable("cmd");

            List<String> allArgs = builder.getArgs();
            builder.setArgs(Arrays.asList("/c", cmd));
            builder.args(allArgs);

            String gradleHome = getBuildContext().getGradleHomeDir().getAbsolutePath();

            // NOTE: Windows uses Path, but allows asking for PATH, and PATH
            //       is set within builder object for some things such
            //       as CommandLineIntegrationTest, try PATH first, and
            //       then revert to default of Path if null
            Object path = builder.getEnvironment().get("PATH");
            if (path == null) {
                path = builder.getEnvironment().get("Path");
            }
            path = String.format("%s\\bin;%s", gradleHome, path);
            builder.environment("PATH", path);
            builder.environment("Path", path);
            builder.environment("GRADLE_EXIT_CONSOLE", "true");
        }
    }

    private class UnixConfigurer implements ExecHandlerConfigurer {
        public void configure(ExecHandleBuilder builder) {
            if (getExecutable() != null) {
                File exe = new File(getExecutable());
                if (exe.isAbsolute()) {
                    builder.executable(exe.getAbsolutePath());
                } else {
                    builder.executable(String.format("%s/%s", getWorkingDir().getAbsolutePath(), getExecutable()));
                }
            } else {
                builder.executable(String.format("%s/bin/gradle", getBuildContext().getGradleHomeDir().getAbsolutePath()));
            }
        }
    }
}
