package de.aaschmid.gradle.plugins.cpd.test;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import de.aaschmid.gradle.plugins.cpd.CpdExtension;
import de.aaschmid.gradle.plugins.cpd.CpdPlugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class GradleExtension implements BeforeEachCallback, ParameterResolver {

    private static final Map<Class<?>, Function<GradleExtension, ?>> parameterMap =
            ImmutableMap.<Class<?>, Function<GradleExtension, ?>>builder()
                    .put(Project.class, (GradleExtension ex) -> ex.project)
                    .put(Configuration.class, (GradleExtension ex) -> ex.project.getConfigurations().getByName("cpd"))
                    .put(CpdExtension.class, (GradleExtension ex) -> ex.project.getExtensions().getByType(CpdExtension.class))
                    // Use TaskProvider because of lazy configuration, otherwise additional configuration within in test is not recognized
                    .put(TaskProvider.class, (GradleExtension ex) -> ex.project.getTasks().named("cpdCheck"))
                    .build();
    private Project project;

    @Override
    public void beforeEach(ExtensionContext context) {
        project = ProjectBuilder.builder().build();
        project.getPlugins().apply(CpdPlugin.class);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterMap.containsKey(parameterContext.getParameter().getType());

    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterMap.get(parameterContext.getParameter().getType()).apply(this);
    }
}
