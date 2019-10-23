package de.aaschmid.gradle.plugins.cpd.test;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class GradleExtension implements BeforeEachCallback, ParameterResolver {

    private Project project;
    private Cpd cpd;

    private static final Map<Class<?>, Function<GradleExtension, ?>> parameterMap =
            ImmutableMap.<Class<?>, Function<GradleExtension, ?>>builder()
                    .put(Project.class, (GradleExtension ex) -> ex.project)
                    .put(Cpd.class, (GradleExtension ex) -> ex.cpd)
                    .build();


    @Override
    public void beforeEach(ExtensionContext context) {
        project = ProjectBuilder.builder().build();
        project.getPlugins().apply(CpdPlugin.class);

        cpd = (Cpd) project.getTasks().getByName("cpdCheck");
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
