package de.aaschmid.gradle.plugins.cpd.internal;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Namer;
import org.gradle.api.Project;
import org.gradle.api.Rule;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.specs.Spec;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class Reports<T extends Report> implements ReportContainer<T> {

    private final NamedDomainObjectSet<T> reports;
    private final NamedDomainObjectSet<T> enabled;

    public Reports(final Project project, final Class<T> clazz) {
        this.reports = project.getObjects().namedDomainObjectSet(clazz);
        this.enabled = this.reports.matching(report -> report.getRequired().get());
    }

    @Override
    public NamedDomainObjectSet<T> getEnabled() {
        return this.enabled;
    }

    protected boolean addReport(final T report) {
        return this.reports.add(report);
    }

    @Override
    public boolean add(final T report) {
        throw new ImmutableViolationException();
    }

    @Override
    public boolean addAll(final Collection<? extends T> reps) {
        throw new ImmutableViolationException();
    }

    @Override
    public void addLater(final Provider<? extends T> provider) {
        throw new ImmutableViolationException();
    }

    @Override
    public void addAllLater(final Provider<? extends Iterable<T>> provider) {
        throw new ImmutableViolationException();
    }

    @Override
    public boolean remove(final Object report) {
        throw new ImmutableViolationException();
    }

    @Override
    public boolean removeAll( final Collection<?> reps) {
        throw new ImmutableViolationException();
    }

    @Override
    public boolean retainAll( final Collection<?> reps) {
        throw new ImmutableViolationException();
    }

    @Override
    public void clear() {
        throw new ImmutableViolationException();
    }

    @Override
    public boolean containsAll( final Collection<?> reps) {
        return this.reports.containsAll(reps);
    }

    @Override
    public Namer<T> getNamer() {
        return Report::getName;
    }

    @Override
    public SortedMap<String, T> getAsMap() {
        return this.reports.getAsMap();
    }

    @Override
    public SortedSet<String> getNames() {
        return this.reports.getNames();
    }

    @Override
    public T findByName(final String name) {
        return this.reports.findByName(name);
    }

    @Override
    public T getByName(final String name) throws UnknownDomainObjectException {
        return this.reports.getByName(name);
    }

    @Override
    public T getByName(final String name, final Closure configureClosure) throws UnknownDomainObjectException {
        return this.reports.getByName(name, configureClosure);
    }

    @Override
    public T getByName(final String name, final Action<? super T> configureAction) throws UnknownDomainObjectException {
        return this.reports.getByName(name, configureAction);
    }

    @Override
    public T getAt(final String name) throws UnknownDomainObjectException {
        return this.reports.getAt(name);
    }

    @Override
    public Rule addRule(final Rule rule) {
        return this.reports.addRule(rule);
    }

    @Override
    public Rule addRule(final String description, final Closure ruleAction) {
        return this.reports.addRule(description, ruleAction);
    }

    @Override
    public Rule addRule(final String description, final Action<String> ruleAction) {
        return this.reports.addRule(description, ruleAction);
    }

    @Override
    public List<Rule> getRules() {
        return this.reports.getRules();
    }

    @Override
    public int size() {
        return this.reports.size();
    }

    @Override
    public boolean isEmpty() {
        return this.reports.isEmpty();
    }

    @Override
    public boolean contains(final Object report) {
        return this.reports.contains(report);
    }

    @Override
    public Iterator<T> iterator() {
        return this.reports.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.reports.toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray( final T1[] arr) {
        return (T1[])this.reports.toArray((T[])arr);
    }

    @Override
    public Map<String, T> getEnabledReports() {
        return this.enabled.getAsMap();
    }

    @Override
    public <S extends T> NamedDomainObjectSet<S> withType(final Class<S> type) {
        return this.reports.withType(type);
    }

    @Override
    public <S extends T> DomainObjectCollection<S> withType(final Class<S> type, final Action<? super S> configureAction) {
        return this.reports.withType(type, configureAction);
    }

    @Override
    public <S extends T> DomainObjectCollection<S> withType(final Class<S> type, final Closure configureClosure) {
        return this.reports.withType(type, configureClosure);
    }

    @Override
    public NamedDomainObjectSet<T> matching(final Spec<? super T> spec) {
        return this.reports.matching(spec);
    }

    @Override
    public NamedDomainObjectSet<T> matching(final Closure spec) {
        return this.reports.matching(spec);
    }

    @Override
    public Action<? super T> whenObjectAdded(final Action<? super T> action) {
        return this.reports.whenObjectAdded(action);
    }

    @Override
    public void whenObjectAdded(final Closure action) {
        this.reports.whenObjectAdded(action);
    }

    @Override
    public Action<? super T> whenObjectRemoved(final Action<? super T> action) {
        return this.reports.whenObjectRemoved(action);
    }

    @Override
    public void whenObjectRemoved(final Closure action) {
        this.reports.whenObjectRemoved(action);
    }

    @Override
    public void all(final Action<? super T> action) {
        this.reports.all(action);
    }

    @Override
    public void all(final Closure action) {
        this.reports.all(action);
    }

    @Override
    public void configureEach(final Action<? super T> action) {
        this.reports.configureEach(action);
    }

    @Override
    public NamedDomainObjectProvider<T> named(final String name) throws UnknownDomainObjectException {
        return this.reports.named(name);
    }

    @Override
    public NamedDomainObjectProvider<T> named(final String name, final Action<? super T> configurationAction) throws UnknownDomainObjectException {
        return this.reports.named(name, configurationAction);
    }

    @Override
    public <S extends T> NamedDomainObjectProvider<S> named(final String name, final Class<S> type) throws UnknownDomainObjectException {
        return this.reports.named(name, type);
    }

    @Override
    public <S extends T> NamedDomainObjectProvider<S> named(final String name, final Class<S> type, final Action<? super S> configurationAction) throws UnknownDomainObjectException {
        return this.reports.named(name, type, configurationAction);
    }

    @Override
    public NamedDomainObjectCollectionSchema getCollectionSchema() {
        return this.reports.getCollectionSchema();
    }

    @Override
    public Set<T> findAll(final Closure spec) {
        return this.reports.findAll(spec);
    }

    @Override
    public ReportContainer<T> configure(final Closure closure) {
        final Closure cl = (Closure) closure.clone();
        cl.setResolveStrategy(Closure.DELEGATE_FIRST);
        cl.setDelegate(this);
        cl.call(this);
        return this;
    }
}
