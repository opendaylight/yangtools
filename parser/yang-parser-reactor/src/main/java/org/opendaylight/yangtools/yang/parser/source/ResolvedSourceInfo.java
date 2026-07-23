/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.model.spi.source.SourceRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;

/**
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct linkage substatements like imports, includes, belongs-to etc...
*/
@NonNullByDefault
public abstract sealed class ResolvedSourceInfo implements Immutable {
    /**
     * A {@link ResolvedSourceInfo} for a {@link SourceInfoRef.OfModule}.
     */
    public static final class Module extends ResolvedSourceInfo {
        private final SourceInfoRef.OfModule infoRef;

        Module(final SourceInfoRef.OfModule infoRef, final List<ResolvedImport> imports,
                final List<ResolvedInclude> includes) {
            super(imports, includes);
            this.infoRef = requireNonNull(infoRef);
        }

        @Override
        public SourceInfoRef.OfModule infoRef() {
            return infoRef;
        }

        @Override
        public Unqualified prefix() {
            return infoRef.info().prefix();
        }

        @Override
        public QNameModule definingModule() {
            return infoRef.info().moduleName().getModule();
        }

        @Override
        public int hashCode() {
            return infoRef.hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return obj == this || obj instanceof Module other && infoRef.equals(other.infoRef);
        }
    }

    /**
     * A {@link ResolvedSourceInfo} for a {@link SourceInfoRef.OfSubmodule}.
     */
    public static final class Submodule extends ResolvedSourceInfo {
        private final SourceInfoRef. OfSubmodule infoRef;
        private final ResolvedBelongsTo belongsTo;

        Submodule(final SourceInfoRef.OfSubmodule infoRef, final ResolvedBelongsTo belongsTo,
                final List<ResolvedImport> imports, final List<ResolvedInclude> includes) {
            super(imports, includes);
            this.infoRef = requireNonNull(infoRef);
            this.belongsTo = requireNonNull(belongsTo);

            final var expectedDep = infoRef.info().belongsTo();
            final var actualDep = belongsTo.dependency();
            if (!expectedDep.equals(actualDep)) {
                throw new VerifyException("Expecting " + expectedDep + " actual " + actualDep);
            }
        }

        @Override
        public SourceInfoRef.OfSubmodule infoRef() {
            return infoRef;
        }

        public SourceRef.ToModule belongsToRef() {
            return belongsTo.sourceRef();
        }

        @Override
        public Unqualified prefix() {
            return belongsTo.dependency().prefix();
        }

        @Override
        public QNameModule definingModule() {
            return belongsTo.parentModuleQname();
        }

        @Override
        public int hashCode() {
            return infoRef.hashCode() + belongsTo.hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return obj == this || obj instanceof Submodule other
                && infoRef.equals(other.infoRef) && belongsTo.equals(other.belongsTo);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("belongsTo", belongsTo);
        }
    }

    /**
     * A builder of {@link ResolvedSourceInfo} instances.
     */
    abstract static sealed class Builder implements Mutable permits ResolvedSourceBuilder {
        /**
         * A set of {@link SourceDependency} objects that need to be resolved to their corresponding {@link Builder}.
         * This class is meant to track {@link Import} and {@link Include}.
         *
         * <p>That separation allows us to assume {@code 0..N} cardinality and shared interpretation
         * of {@link SourceDependency#revision()}: it may or may not be a wildcard.
         *
         * @param <D> dependency type
         * @param <B> {@link Builder} type
         */
        abstract static sealed class Dependencies<D extends SourceDependency, B extends Builder> {
            /**
             * {@return the unmodifiable iterator reporting all dependencies that remain unresolved}
             */
            abstract Iterator<D> missing();

            /**
             * {@return an unmodifiable iterator reporting all dependencies that have been resolved}
             */
            abstract Iterator<B> present();

            /**
             * Resolve a currently-missing dependency with a builder.
             *
             * @param dependency the dependency that is missing
             * @param target the builder to use to resolve the dependency
             * @return replacement {@link Dependencies}, if needed
             */
            final @Nullable Dependencies<D, B> resolveMissing(final @NonNull D dependency,
                    final @NonNull B target) {
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
            abstract @Nullable ResolvedDependencies<D, B> doResolveMissing(@NonNull D dependency, @NonNull B target);

            /**
             * Build a list of objects, each representing a dependency. Implementations of this method assert that all
             * dependencies have been satisfied and reports an exception if {@code #missing().isEmpty()} is known to be
             * {@code false}.
             *
             * @param <R> per-dependency result type
             * @param function the function to turn a dependency and its corresponding builder into the result type
             * @return a list of results
             */
            final <R> List<R> buildResolved(final BiFunction<D, B, R> function) {
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
            abstract <R> List<R> doBuildResolved(BiFunction<D, B, R> function);

            @Override
            public abstract String toString();

            // common utility for doResolveMissing() implementations
            static final VerifyException notMissingException(final SourceDependency dependency, final Builder target,
                    final @Nullable Builder existing) {
                return existing == null
                    // replace failed because the dependency was not specified
                    ? new VerifyException("Attempted to resolve unspecified " + dependency)
                    // replace failed because the dependency was already resolved
                    : new VerifyException("Attempted to override resolution of " + dependency + " from " + existing
                        + " to " + target);
            }
        }

        /**
         * An implementation of {@link Dependencies} indicating there are no dependencies.
         *
         * @param <D> dependency type
         * @param <B> {@link Builder} type
         */
        private static final class NoDependencies<D extends SourceDependency, B extends Builder>
                extends Dependencies<D, B> {
            private static final NoDependencies<?, ?> INSTANCE = new NoDependencies<>();

            private NoDependencies() {
                // hidden on purpose
            }

            @SuppressWarnings("unchecked")
            static <D extends SourceDependency, B extends Builder> NoDependencies<D, B> of() {
                return (NoDependencies<D, B>) INSTANCE;
            }

            @Override
            Iterator<D> missing() {
                return Collections.emptyIterator();
            }

            @Override
            Iterator<B> present() {
                return Collections.emptyIterator();
            }

            @Override
            @Nullable ResolvedDependencies<D, B> doResolveMissing(final D dependency, final B target) {
                throw notMissingException(dependency, target, null);
            }

            @Override
            <R> List<R> doBuildResolved(final BiFunction<D, B, R> function) {
                return List.of();
            }

            @Override
            public String toString() {
                return "NoDependencies{}";
            }
        }

        /**
         * An implementation of {@link Dependencies} indicating there is at least one dependency.
         *
         * @param <D> dependency type
         * @param <B> {@link Builder} type
         */
        private abstract static sealed class SomeDependencies<D extends SourceDependency, B extends Builder>
                extends Dependencies<D, B> {
            @Override
            final <R> List<R> doBuildResolved(final BiFunction<D, B, R> function) {
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

            abstract Map<D, ? extends @Nullable B> map();

            @Override
            public final String toString() {
                return MoreObjects.toStringHelper(this).add("dependencies", map()).toString();
            }
        }

        /**
         * An implementation of {@link SomeDependencies} indicating there is at least one dependency that needs to be
         * resolved. It should eventually settle into {@link ResolvedDependencies}
         *
         * @param <D> dependency type
         * @param <B> {@link Builder} type
         */
        private static final class NeededDependencies<D extends SourceDependency, B extends Builder>
                extends SomeDependencies<D, B> {
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
            private final LinkedHashMap<D, @Nullable B> map;

            private int missing;

            /**
             * Default constructor. Should only be called from {@link Dependencies#of(Set)}.
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
            Map<D, @Nullable B> map() {
                return map;
            }

            @Override
            Iterator<D> missing() {
                return map.entrySet().stream()
                    .filter(entry -> entry.getValue() == null)
                    .map(Map.Entry::getKey)
                    .iterator();
            }

            @Override
            Iterator<B> present() {
                return map.values().stream().filter(Objects::nonNull).iterator();
            }

            @Override
            @Nullable ResolvedDependencies<D, B> doResolveMissing(final D dependency, final B target) {
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
         * resolved. It should eventually settle into {@link ResolvedDependencies}
         *
         * @param <D> dependency type
         * @param <B> {@link Builder} type
         */
        private static final class ResolvedDependencies<D extends SourceDependency, B extends Builder>
                extends SomeDependencies<D, B> {
            private final ImmutableMap<D, B> map;

            ResolvedDependencies(final LinkedHashMap<D, @Nullable B> map) {
                final var builder = ImmutableMap.<D, B>builderWithExpectedSize(map.size());
                for (var entry : map.entrySet()) {
                    @SuppressWarnings("null")
                    final var value = (@NonNull B) verifyNotNull(entry.getValue());
                    builder.put(entry.getKey(), value);
                }
                this.map = builder.build();
            }

            @Override
            ImmutableMap<D, B> map() {
                return map;
            }

            @Override
            Iterator<D> missing() {
                return Collections.emptyIterator();
            }

            @Override
            Iterator<B> present() {
                return map.values().iterator();
            }

            @Override
            @Nullable ResolvedDependencies<D, B> doResolveMissing(final D dependency, final B target) {
                throw notMissingException(dependency, target, map.get(dependency));
            }
        }

        final String humanName() {
            final var sourceId = sourceId();
            return humanName(sourceId.name(), sourceId.revision());
        }

        static final String humanName(final Unqualified name, final @Nullable Revision revision) {
            final var localName = name.getLocalName();
            return revision == null ? localName : localName + "@" + revision;
        }

        final Unqualified name() {
            return sourceId().name();
        }

        final @Nullable Revision revision() {
            return sourceId().revision();
        }

        final SourceIdentifier sourceId() {
            return sourceInfo().sourceId();
        }

        final YangVersion yangVersion() {
            return sourceInfo().yangVersion();
        }

        abstract SourceInfoRef infoRef();

        abstract SourceInfo sourceInfo();

        /**
         * {@return the {@link ResolvedSourceInfo} result of this builder}
         */
        abstract ResolvedSourceInfo build();

        @Override
        public abstract String toString();

        /**
         * {@return a {@link Dependencies} instance for the specified set of initial dependencies}
         * @param <D> dependency type
         * @param <B> {@link Builder} type
         * @param dependencies the set of dependencies
         */
        static final <D extends SourceDependency, B extends Builder> Dependencies<D, B> dependenciesOf(
                final Set<@NonNull D> dependencies) {
            return dependencies.isEmpty() ? NoDependencies.of() : new NeededDependencies<>(dependencies);
        }
    }

    private final List<ResolvedImport> imports;
    private final List<ResolvedInclude> includes;

    private ResolvedSourceInfo(final List<ResolvedImport> imports, final List<ResolvedInclude> includes) {
        this.imports = List.copyOf(imports);
        this.includes = List.copyOf(includes);
    }

    public abstract SourceInfoRef infoRef();

    public abstract QNameModule definingModule();

    public abstract Unqualified prefix();

    public final List<ResolvedImport> imports() {
        return imports;
    }

    public final List<ResolvedInclude> includes() {
        return includes;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("infoRef", infoRef());
    }
}
