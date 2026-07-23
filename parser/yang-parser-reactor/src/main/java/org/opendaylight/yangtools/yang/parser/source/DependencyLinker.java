/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo.Builder;

/**
 * A set of {@link SourceDependency} objects that need to be resolved to their corresponding target. This class is meant
 * to track {@link Import} and {@link Include}.
 *
 * <p>That separation allows us to assume {@code 0..N} cardinality and shared interpretation
 * of {@link SourceDependency#revision()}: it may or may not be a wildcard.
 *
 * @param <D> dependency type
 * @param <T> target type
 */
@NonNullByDefault
abstract sealed class DependencyLinker<D extends SourceDependency, T> {
    /**
     * Internal base class, ensuring consistent implementation behaviour.
     *
     * @param <D> dependency type
     * @param <T> target type
     */
    private abstract static sealed class Base<D extends SourceDependency, T> extends DependencyLinker<D, T> {
        @Override
        final @Nullable DependencyLinker<D, T> resolveMissing(final D dependency, final T target) {
            // split to keep argument checking consistent
            return doResolveMissing(requireNonNull(dependency), requireNonNull(target));
        }

        /**
         * Implementation of the {@link #resolveMissing(SourceDependency, Builder)} contract. All arguments are
         * guaranteed to be non-{@code null} by the caller.
         *
         * @param dependency the dependency that is missing
         * @param target the builder to use to resolve the dependency
         * @return replacement {@link ResolvedDependencies}, if needed
         */
        abstract @Nullable ResolvedDependencies<D, T> doResolveMissing(@NonNull D dependency, @NonNull T target);

        // common utility for doResolveMissing() implementations
        static final VerifyException notMissingException(final SourceDependency dependency, final Object target,
                final @Nullable Object existing) {
            return existing == null
                // replace failed because the dependency was not specified
                ? new VerifyException("Attempted to resolve unspecified " + dependency)
                // replace failed because the dependency was already resolved
                : new VerifyException("Attempted to override resolution of " + dependency + " from " + existing + " to "
                    + target);
        }

        @Override
        final <R> List<R> buildResolved(final BiFunction<D, T, R> function) {
            return doBuildResolved(requireNonNull(function));
        }

        /**
         * Implementation of the {@link #buildResolved(BiFunction)} contract. All arguments are guaranteed to be
         * non-{@code null} by the caller.
         *
         * @param <R> per-dependency result type
         * @param function the function to turn a dependency and its corresponding builder into the result type
         * @return a list of results
         */
        abstract <R> List<R> doBuildResolved(BiFunction<D, T, R> funtction);

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
        }

        abstract ToStringHelper addToStringAttributes(ToStringHelper helper);
    }

    /**
     * An implementation of {@link DependencyLinker} indicating there are no dependencies.
     *
     * @param <D> dependency type
     * @param <T> target type
     */
    private static final class NoDependencies<D extends SourceDependency, T> extends Base<D, T> {
        private static final NoDependencies<?, ?> INSTANCE = new NoDependencies<>();

        private NoDependencies() {
            // hidden on purpose
        }

        @SuppressWarnings("unchecked")
        static <D extends SourceDependency, T> NoDependencies<D, T> of() {
            return (NoDependencies<D, T>) INSTANCE;
        }

        @Override
        Iterator<D> missing() {
            return Collections.emptyIterator();
        }

        @Override
        Iterator<T> present() {
            return Collections.emptyIterator();
        }

        @Override
        @Nullable ResolvedDependencies<D, T> doResolveMissing(final D dependency, final T target) {
            throw notMissingException(dependency, target, null);
        }

        @Override
        <R> List<R> doBuildResolved(final BiFunction<D, T, R> function) {
            return List.of();
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper;
        }
    }

    /**
     * An implementation of {@link DependencyLinker} indicating there is at least one dependency.
     *
     * @param <D> dependency type
     * @param <T> target type
     */
    private abstract static sealed class SomeDependencies<D extends SourceDependency, T> extends Base<D, T> {
        @Override
        final <R> List<R> doBuildResolved(final BiFunction<D, T, R> function) {
            final var entries = map().entrySet();
            final var tmp = new ArrayList<R>(entries.size());
            for (var entry : entries) {
                final var dependency = entry.getKey();
                final var resolved = entry.getValue();
                if (resolved == null) {
                    throw new VerifyException("Unresolved dependency " + dependency);
                }
                tmp.add(verifyNotNull(function.apply(dependency, resolved)));
            }
            return List.copyOf(tmp);
        }

        abstract Map<D, ? extends @Nullable T> map();

        @Override
        final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("dependencies", map());
        }
    }

    /**
     * An implementation of {@link SomeDependencies} indicating there is at least one dependency that needs to be
     * resolved. It should eventually settle into {@link ResolvedDependencies}
     *
     * @param <D> dependency type
     * @param <T> target type
     */
    private static final class NeededDependencies<D extends SourceDependency, T> extends SomeDependencies<D, T> {
        /**
         * The map of dependencies. The iteration order matches the iteration order of specified dependencies
         * and contains {@code null} for each dependency. This allows us to enforce that each dependency is resolved
         * exactly once (by making a {@code null} to non-{@code null} transition), at which it becomes satisfied --
         * thus providing a combined capability to detect
         * <ol>
         *   <li>attempts to resolve a dependency more than once, as well as</li>
         *   <li>attempts to resolve a dependency that was not specified</li>
         * </ol>
         */
        private final LinkedHashMap<D, @Nullable T> map;

        private int missing;

        /**
         * Default constructor. Should only be called from {@link DependencyLinker#of(Set)}.
         *
         * @param dependencies the set of dependencies
         */
        NeededDependencies(final Set<D> dependencies) {
            map = LinkedHashMap.newLinkedHashMap(dependencies.size());
            for (var dependency : dependencies) {
                map.put(requireNonNull(dependency), null);
            }
            missing = map.size();
        }

        @Override
        Map<D, @Nullable T> map() {
            return map;
        }

        @Override
        Iterator<D> missing() {
            return map.entrySet().stream().filter(entry -> entry.getValue() == null).map(Map.Entry::getKey).iterator();
        }

        @Override
        Iterator<T> present() {
            return map.values().stream().filter(Objects::nonNull).iterator();
        }

        @Override
        @Nullable ResolvedDependencies<D, T> doResolveMissing(final D dependency, final T target) {
            if (!map.replace(dependency, null, target)) {
                throw notMissingException(dependency, target, map.get(dependency));
            }
            final var local = missing;
            return switch (local) {
                case 0 -> throw new VerifyException("Mismatched " + local + " and " + map);
                case 1 -> {
                    final var resolved = new ResolvedDependencies<>(map);
                    missing = 0;
                    yield resolved;
                }
                default -> {
                    missing = local - 1;
                    yield null;
                }
            };
        }
    }

    /**
     * An implementation of {@link SomeDependencies} indicating there is at least one dependency that needs to be
     * resolved.
     *
     * @param <D> dependency type
     * @param <T> target type
     */
    private static final class ResolvedDependencies<D extends SourceDependency, T> extends SomeDependencies<D, T> {
        private final ImmutableMap<D, T> map;

        ResolvedDependencies(final LinkedHashMap<D, @Nullable T> map) {
            final var builder = ImmutableMap.<D, T>builderWithExpectedSize(map.size());
            for (var entry : map.entrySet()) {
                @SuppressWarnings("null")
                final var value = (@NonNull T) verifyNotNull(entry.getValue());
                builder.put(entry.getKey(), value);
            }
            this.map = builder.build();
        }

        @Override
        ImmutableMap<D, T> map() {
            return map;
        }

        @Override
        Iterator<D> missing() {
            return Collections.emptyIterator();
        }

        @Override
        Iterator<T> present() {
            return map.values().iterator();
        }

        @Override
        @Nullable ResolvedDependencies<D, T> doResolveMissing(final D dependency, final T target) {
            throw notMissingException(dependency, target, map.get(dependency));
        }
    }

    /**
     * {@return a {@link DependencyLinker} instance for the specified set of initial dependencies}
     * @param <D> dependency type
     * @param <T> target type
     * @param dependencies the set of dependencies
     */
    static final <D extends SourceDependency, T> DependencyLinker<D, T> of(final Set<@NonNull D> dependencies) {
        return dependencies.isEmpty() ? NoDependencies.of() : new NeededDependencies<>(dependencies);
    }

    /**
     * {@return the unmodifiable iterator reporting all dependencies that remain unresolved}
     */
    abstract Iterator<D> missing();

    /**
     * {@return an unmodifiable iterator reporting all dependencies that have been resolved}
     */
    abstract Iterator<T> present();

    /**
     * Resolve a currently-missing dependency with a builder.
     *
     * @param dependency the dependency that is missing
     * @param target the builder to use to resolve the dependency
     * @return replacement {@link DependencyLinker}, if needed
     */
    abstract @Nullable DependencyLinker<D, T> resolveMissing(D dependency, T target);

    /**
     * Build a list of objects, each representing a dependency. Implementations of this method assert that all
     * dependencies have been satisfied and reports an exception if {@code #missing().isEmpty()} is known to be
     * {@code false}.
     *
     * @param <R> per-dependency result type
     * @param function the function to turn a dependency and its corresponding builder into the result type
     * @return a list of results
     */
    abstract <R> List<R> buildResolved(BiFunction<D, T, R> function);

    @Override
    public abstract String toString();
}