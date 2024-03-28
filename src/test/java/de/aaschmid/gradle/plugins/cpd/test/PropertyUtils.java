package de.aaschmid.gradle.plugins.cpd.test;

import java.util.List;

import org.gradle.api.internal.provider.DefaultListProperty;
import org.gradle.api.internal.provider.DefaultProperty;
import org.gradle.api.internal.provider.PropertyHost;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public class PropertyUtils {

    public static <T> Property<T> property(T t) {
        @SuppressWarnings("unchecked")
        Property<T> property = new DefaultProperty<>(PropertyHost.NO_OP, (Class<T>) t.getClass());
        property.set(t);
        return property;
    }

    public static <T> ListProperty<T> listProperty(Class<T> clazz, List<T> list) {
        ListProperty<T> listProperty = new DefaultListProperty<>(PropertyHost.NO_OP, clazz);
        listProperty.set(list);
        return listProperty;
    }
}
