/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.VerifyException;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.RevisionUnion;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.DeclarationInSource;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

/**
 * The state required to construct a {@link ResolvedSourceInfo} for a particular {@link SourceInfoRef}. There should be
 * exactly one instance of this class for each {@link SourceInfoRef} in a particular {@link SourceLinkageResolver}
 * instance.
 *
 * <p>This class is marked as {@link Mutable} because it has internal state, but once {@link #build()} succeeds, it
 * really becomes immutable, rejecting any state modification and returning the same build result.
 *
 * <p>This class is an implementation detail of {@link SourceLinkageResolver} and is expected to be used in the context
 * of a single thread executing {@link SourceLinkageResolver#resolveInvolvedSources(Set, Set)}.
 */
// TODO: this is not exactly a builder, as the build() product is explicitly retained and we update the state tracked
//       here as we resolve inter-dependencies. A better name would be 'SourceLinkage' or something similar.
// TODO: reconsider subclass naming: we are always referring to the types in qualified fashion, so SourceLinkage.Module
//       should work nicely while taking up fewer characters.
abstract sealed class ResolvedSourceBuilder<R extends SourceInfoRef> implements Mutable {
    /**
     * A {@link ResolvedSourceBuilder} for a YANG {@code module}. It provides a meeting point for resolving
     * {@code include} statements to a consistent set of sources, such that violations of RFC6020/RFC7950 section 7.1.6
     * requirement that {@code Multiple revisions of the same submodule MUST NOT be included.} are reliably reported.
     */
    @NonNullByDefault
    static final class ForModule extends ResolvedSourceBuilder<SourceInfoRef.OfModule> {
        /**
         * The source of an {@link ExactRevision}.
         */
        private sealed interface Exactness {
            /**
             * Exact revision spelled out in source.
             */
            record Explicit(StatementSourceReference sourceRef) implements Exactness {
                public Explicit {
                    requireNonNull(sourceRef);
                }

                @Override
                public String sourceString() {
                    return switch (sourceRef) {
                        case DeclarationInSource ref -> ref.toString();
                        case StatementDeclaration ref -> "at " + ref;
                        default -> "from " + sourceRef;
                    };
                }
            }

            /**
             * Exact revision inferred during resolution.
             */
            final class Narrowed implements Exactness {
                static final Narrowed INSTANCE = new Narrowed();

                private Narrowed() {
                    // hidden on purpose
                }

                @Override
                public String sourceString() {
                    return "(narrowed)";
                }
            }

            String sourceString();
        }

        /**
         * The specification of how the source of a submodule should be looked up.
         */
        // TODO: consider promoting parts of this contract to model.spi.source.SourceDependency
        private sealed interface SubmoduleSpec {
            // just a marker
        }

        /**
         * A {@link SubmoduleSpec} lacking a revision specification, e.g. {@code include foo;}.
         */
        private static final class AnyRevision implements SubmoduleSpec {
            static final AnyRevision INSTANCE = new AnyRevision();

            private AnyRevision() {
                // hidden on purpose
            }
        }

        /**
         * A {@link SubmoduleSpec} with a revision specification, either by means of being explicit
         * {@code include foo { revision-date 1970-01-01; }}, or by means of being narrowed during resolution.
         */
        // TODO: abstract sealed record when Java provides them as hinted at in
        //       https://youtu.be/BdLND9D81lI?si=tAX8gXsPC1FBh5tJ&t=1703
        private sealed interface ExactRevision extends SubmoduleSpec {

            RevisionUnion revision();

            Exactness exactness();
        }

        /**
         * An {@link ExactRevision} which has been resolved to a {@link ForSubmodule}.
         */
        private record ResolvedRevision(ForSubmodule submodule, Exactness exactness) implements ExactRevision {
            ResolvedRevision {
                requireNonNull(submodule);
                requireNonNull(exactness);
            }

            @Override
            public RevisionUnion revision() {
                return RevisionUnion.of(submodule.revision());
            }
        }

        /**
         * An {@link ExactRevision} which has not been resolved.
         */
        private record UnresolvedRevision(RevisionUnion revision, Exactness exactness) implements ExactRevision {
            UnresolvedRevision {
                requireNonNull(revision);
                requireNonNull(exactness);
            }

            ResolvedRevision toResolved(final ForSubmodule submodule) {
                if (!Objects.equals(revision.revision(), submodule.revision())) {
                    throw new VerifyException("Attempted to resolve " + this + " with " + submodule.humanName());
                }
                return new ResolvedRevision(submodule, exactness);
            }
        }

        /**
         * The set of names of known submodules and their corresponding {@link SubmoduleSpec}.
         *
         * <p>RFC6020 and RFC7950 define different semantics on how submodules are included:
         * <ul>
         *   <li>RFC6020 requires recursive resolution of an acyclic graph of include statements</li>
         *   <li>RFC7950 requires all submodules to be included from the parent module and allows submodules' includes
         *       to form cycles</li>
         * </ul>
         * In both cases the effective set of included submodules must contain exactly one source for each submodule
         * name, so that is what we are tracking.
         */
        private final LinkedHashMap<Unqualified, SubmoduleSpec> submoduleSpecs = new LinkedHashMap<>();

        ForModule(final SourceInfoRef.OfModule infoRef) throws ReactorException {
            super(infoRef);
            requireIncludes(this);
        }

        /**
         * {@return the set names of submodules required by this module and lack an exact revision specification}
         */
        Set<Unqualified> inexactSubmodules() {
            return Maps.filterValues(submoduleSpecs, AnyRevision.class::isInstance).keySet();
        }

        void narrowInexact(final Unqualified submodule, final RevisionUnion revision) {
            verify(submoduleSpecs.replace(submodule, AnyRevision.INSTANCE,
                new UnresolvedRevision(revision, Exactness.Narrowed.INSTANCE)));
        }

        /**
         * {@return the {@link RevisionUnion} of a submodule that is known to be required by this module, but has not
         * been resolved yet, or {@code null} if the submodule has been resolved or the revision is not yet known}
         * @param submodule submodule name
         */
        @Nullable RevisionUnion lookupRevision(final Unqualified submodule) {
            return switch (submoduleSpecs.get(requireNonNull(submodule))) {
                case null -> throw new VerifyException("Unexpected submodule " + submodule.getLocalName());
                case UnresolvedRevision needed -> needed.revision;
                default -> null;
            };
        }

        /**
         * {@return the memoized {@link ForSubmodule}, or {@code null} if the submodule was not yet resolved}
         * @param submodule submodule name
         */
        @Nullable ForSubmodule lookupSubmodule(final Unqualified submodule) {
            return submoduleSpecs.get(requireNonNull(submodule)) instanceof ResolvedRevision resolved
                ? resolved.submodule : null;
        }

        /**
         * Resolve the requirement to include a particular submodule revision, reported by
         * {@link #lookupRevision(Unqualified)}, to a particular {@link ForSubmodule}.
         *
         * @param submodule the submodule
         */
        void resolveSubmodule(final ForSubmodule submodule) {
            final var sourceId = submodule.sourceId();
            final var name = sourceId.name();
            final var spec = submoduleSpecs.get(name);
            switch (spec) {
                case null -> throw new VerifyException("Unexpected submodule " + name.getLocalName());
                case UnresolvedRevision prev -> verify(submoduleSpecs.replace(name, prev, prev.toResolved(submodule)));
                default -> throw new VerifyException("Attempted to resolve " + spec + " with " + submodule.humanName());
            }
        }

        /**
         * Record a requirement for this module to {@code Include} a set of submodule implied by a source.
         *
         * @param source the {@link ResolvedSourceBuilder} to the source of requirements
         * @throws ReactorException if a requirement conflicts with a previous requirement
         */
        private void requireIncludes(final ResolvedSourceBuilder<?> source) throws ReactorException {
            final var it = source.missingIncludes().iterator();
            if (it.hasNext()) {
                do {
                    requireInclude(source, it.next());
                } while (it.hasNext());
            }
        }

        /**
         * Record a requirement for this module to {@code Include} a submodule.
         *
         * @param source the {@link ResolvedSourceBuilder} of requirements
         * @param dependency the {@link Include}
         * @param rejectNew new include rejector, or {@code null}
         * @throws ReactorException if the requirement conflicts with a previous requirement or cannot be added
         */
        private void requireInclude(final ResolvedSourceBuilder<?> source, final Include dependency)
                throws ReactorException {
            final var name = dependency.name();
            final var revision = dependency.revision();
            if (revision == null) {
                // unspecified revision, never conflicts
                if (!submoduleSpecs.containsKey(name)) {
                    checkInclude(source, dependency);
                    submoduleSpecs.put(name, AnyRevision.INSTANCE);
                }
                return;
            }

            // FIXME: Java 25: merge the two cases below when we can say 'case AnyRevision _' and move this allocation
            //        there
            final var depRef = dependency.sourceRef();
            final var sourceRef = depRef != null ? depRef : source.sourceId().toReference();
            final var spec = new UnresolvedRevision(revision, new Exactness.Explicit(sourceRef));

            // yes, we have a Map.get() + Map.put() and could be written as a Map.compute() operation, but this way is
            // actually more modern: we are using Java 21+ language features instead of Java 8+ java.util features
            // we do not care about the two HashMap lookup operations.
            switch (submoduleSpecs.get(name)) {
                case null -> {
                    checkInclude(source, dependency);
                    submoduleSpecs.put(name, spec);
                }
                case AnyRevision any -> submoduleSpecs.put(name, spec);
                case ExactRevision exact -> {
                    final var exactRevision = exact.revision();
                    if (!revision.equals(exactRevision)) {
                        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE,
                            source.sourceId(), new InferenceException(sourceRef, """
                                Cannot include-by-revision submodule %s in module %s: already included as %s %s""",
                                humanName(name, revision), humanName(), humanName(name, exactRevision.revision()),
                                exact.exactness().sourceString()));
                    }
                }
            }
        }

        @Override
        SourceInfo.Module sourceInfo() {
            return infoRef().info();
        }

        @Override
        ResolvedSourceInfo.Module buildProduct(final List<ResolvedImport> resolvedImports,
                final List<ResolvedInclude> resolveIncludes) {
            return new ResolvedSourceInfo.Module(infoRef(), resolvedImports, resolveIncludes);
        }

        /**
         * Check that a source can add an {@link Include} dependency to this module.
         *
         * @param source the source that is resolving the dependency
         * @param dependency the dependency being resolved
         * @throws ReactorException if the source cannot be add the dependency to this module
         */
        void checkInclude(final ResolvedSourceBuilder<?> source, final Include dependency) throws ReactorException {
            switch (source) {
                case ForModule module -> verify(module == this);
                case ForSubmodule submodule -> {
                    final var yangVersion = yangVersion();
                    if (yangVersion != YangVersion.VERSION_1) {
                        final var depRef = dependency.sourceRef();
                        final var sourceId = sourceId();
                        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                            new InferenceException(depRef != null ? depRef : sourceId.toReference(), """
                                Parent module %s does not include %s, YANG %s does not allow it to be included from \
                                submodule %s""", humanName(), dependency.name(), yangVersion, submodule.humanName()));
                    }
                }
            }
        }
    }

    /**
     * A {@link ResolvedSourceBuilder} for a YANG {@code submodule}.
     */
    static final class ForSubmodule extends ResolvedSourceBuilder<SourceInfoRef.OfSubmodule> {
        // FIXME: internal state here: we go from unresolved -> resolved -> built, and we would like to throw away
        //        internal state when the product is built
        private @Nullable ForModule parentModule;

        @NonNullByDefault
        ForSubmodule(final SourceInfoRef.OfSubmodule infoRef) {
            super(infoRef);
        }

        /**
         * {@return the {@link ForModule} corresponding to the parent module, or {@code null} if not yet determined}
         */
        @Nullable ForModule parentModule() {
            return parentModule;
        }

        @Override
        SourceInfo.Submodule sourceInfo() {
            return infoRef().info();
        }

        @Override
        boolean isResolved() {
            return parentModule != null && super.isResolved();
        }

        @NonNullByDefault
        void resolveBelongsTo(final ForModule module) throws ReactorException {
            // order of operations has implications on error reporting:
            // - we reject duplicate resolution, then
            // - we reject mismatch between proposed module and belongs-to module name, then
            // - we declare belongs-to resolved, and finally
            // - we inform the module of the include dependencies this submodule brings to the table
            verifyNoParentModule(module);
            final var belongsTo = sourceInfo().belongsTo().name();
            if (!belongsTo.equals(module.name())) {
                throw new VerifyException("Attempted to resolve belongs-to " + belongsTo.getLocalName()
                    + " with module " + module.humanName());
            }
            parentModule = module;
            module.requireIncludes(this);
        }

        /**
         * Adds a {@link ForModule} of the parent module this submodule belongs to.
         *
         * @param dependency the {@link BelongsTo} being satistifed
         * @param link {@link ForModule} of the parent module.
         */
        @NonNullByDefault
        void resolveBelongsTo(final BelongsTo dependency, final ForModule module) {
            verifyNoParentModule(module);
            parentModule = requireNonNull(module);
        }

        @Override
        ResolvedSourceInfo.Submodule buildProduct(final List<@NonNull ResolvedImport> resolvedImports,
                final List<@NonNull ResolvedInclude> resolveIncludes) {
            final var local = parentModule;
            if (local == null) {
                throw new VerifyException("Unresolved belongs-to in " + this);
            }
            final var parentRef = local.infoRef();
            final var infoRef = infoRef();
            return new ResolvedSourceInfo.Submodule(infoRef,
                new ResolvedBelongsTo(infoRef.info().belongsTo(), parentRef.ref(),
                    parentRef.info().moduleName().getModule()), resolvedImports, resolveIncludes);
        }

        @NonNullByDefault
        private void verifyNoParentModule(final ForModule module) {
            final var local = parentModule;
            if (local != null) {
                throw new VerifyException("Attempted to re-resolve belongs-to from " + local + " to " + module);
            }
        }
    }

    /**
     * A set of {@link SourceDependency} objects that need to be resolved to their corresponding
     * {@link ResolvedSourceBuilder}. This class is meant to track {@link Import} and {@link Include}, with
     * {@link ForSubmodule} tracking {@link BelongsTo} separately.
     *
     * <p>That separation allows us to assume {@code 0..N} cardinality and shared interpretation
     * of {@link SourceDependency#revision()}: it may or may not be a wildcard.
     *
     * @param <D> dependency type
     * @param <B> {@link ResolvedSourceBuilder} type
     */
    @NonNullByDefault
    private abstract static sealed class Dependencies<D extends SourceDependency, B extends ResolvedSourceBuilder<?>> {
        /**
         * {@return an instance for the specified set of initial dependencies}
         * @param <D> dependency type
         * @param <B> {@link ResolvedSourceBuilder} type
         * @param dependencies the set of dependencies
         */
        static final <D extends SourceDependency, B extends ResolvedSourceBuilder<?>> Dependencies<D, B> of(
                final Set<@NonNull D> dependencies) {
            return dependencies.isEmpty() ? NoDependencies.of() : new SomeDependencies<>(dependencies);
        }

        /**
         * {@return the unmodifiable view of dependencies that remain unresolved. Guaranteed to be updated by
         * {@link #resolveMissing(SourceDependency, SourceInfoRef)} invocations and iterators reporting
         * {@link ConcurrentModificationException}}
         */
        abstract Set<@NonNull D> missing();

        abstract Iterator<@NonNull B> present();

        /**
         * Resolve a currently-missing dependency with a builder.
         *
         * @param dependency the dependency that is missing
         * @param target the builder to use to resolve the dependency
         */
        final void resolveMissing(final @NonNull D dependency, final @NonNull B target) {
            // split to keep argument checking consistent
            doResolveMissing(requireNonNull(dependency), requireNonNull(target));
        }

        /**
         * Implementation of the {@link #resolveMissing(SourceDependency, SourceInfoRef)} contract. All arguments are
         * guaranteed to be non-{@code null} by the caller.
         *
         * @param dependency the dependency that is missing
         * @param target the builder to use to resolve the dependency
         */
        abstract void doResolveMissing(@NonNull D dependency, @NonNull B target);

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
    }

    /**
     * An implementation of {@link Dependencies} indicating there are no dependencies.
     *
     * @param <D> dependency type
     * @param <B> {@link ResolvedSourceBuilder} type
     */
    @NonNullByDefault
    private static final class NoDependencies<D extends SourceDependency, B extends ResolvedSourceBuilder<?>>
            extends Dependencies<D, B> {
        private static final NoDependencies<?, ?> INSTANCE = new NoDependencies<>();

        private NoDependencies() {
            // hidden on purpose
        }

        @SuppressWarnings("unchecked")
        static <D extends SourceDependency, B extends ResolvedSourceBuilder<?>> NoDependencies<D, B> of() {
            return (NoDependencies<D, B>) INSTANCE;
        }

        @Override
        Set<D> missing() {
            return Set.of();
        }

        @Override
        Iterator<B> present() {
            return Collections.emptyIterator();
        }

        @Override
        void doResolveMissing(final D dependency, final B target) {
            throw new VerifyException("Attempted to resolve unspecified " + dependency);
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
     * @param <B> {@link ResolvedSourceBuilder} type
     */
    @NonNullByDefault
    private static final class SomeDependencies<D extends SourceDependency, B extends ResolvedSourceBuilder<?>>
            extends Dependencies<D, B> {
        /**
         * The map of dependencies. The iteration order matches the iteration order of specified dependencies
         * and contains {@code null} for each dependency. This allows us to enforce that each dependency is resolved
         * exactly once (by making a {@code null} to non-{@code null} transition), at which it becomes satisfied -- thus
         * providing a combined capability to detect
         * <ol>
         *   <li>attempts to resolve a dependency more than once, as well as</li>
         *   <li>attempts to resolve a dependency that was not specified</li>
         * </ol>
         */
        private final LinkedHashMap<D, @Nullable B> map;
        /**
         * An unmodifiable view on what dependencies have not been satisfied. This is a materialized view
         * of {@code map.keySet()} filtering out any entries which have a {@code null} value.
         *
         * <p>The set's iterators do not throw {@link ConcurrentModificationException} because the {@link #map} is not
         * structurally modified by {@link #doResolveMissing(SourceDependency, ResolvedSourceBuilder)}, but users need
         * to have a well-defined relationship between iterator advancement and the calls to that method.
         */
        private final Set<D> missing;

        /**
         * Default constructor. Should only be called from {@link Dependencies#of(Set)}.
         *
         * @param dependencies the set of dependencies
         */
        SomeDependencies(final Set<D> dependencies) {
            map = LinkedHashMap.newLinkedHashMap(dependencies.size());
            for (var dependency : dependencies) {
                map.put(requireNonNull(dependency), null);
            }
            missing = Collections.unmodifiableSet(Maps.filterValues(map, Objects::isNull).keySet());
        }

        @Override
        Set<D> missing() {
            return missing;
        }

        @Override
        Iterator<B> present() {
            return map.values().stream().filter(Objects::nonNull).iterator();
        }

        @Override
        void doResolveMissing(final D dependency, final B target) {
            if (!map.replace(dependency, null, target)) {
                final var prev = map.get(dependency);
                throw prev == null
                    // replace failed because the dependency was not specified
                    ? new VerifyException("Attempted to resolve unspecified " + dependency)
                    // replace failed because the dependency was already resolved
                    : new VerifyException(
                        "Attempted to override resolution of " + dependency + " from " + prev + " to " + target);
            }
        }

        @Override
        <R> List<R> doBuildResolved(final BiFunction<D, B, R> function) {
            final var tmp = new ArrayList<R>(map.size());
            for (var entry : map.entrySet()) {
                final var dependency = entry.getKey();
                final var resolved = entry.getValue();
                if (resolved == null) {
                    throw new VerifyException("Unresolved dependency " + dependency);
                }
                tmp.add(verifyNotNull(function.apply(dependency, resolved)));
            }
            return List.copyOf(tmp);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("dependencies", map).toString();
        }
    }

    /**
     * A {@link Dependencies} implementation indicating resolution has been completed and consumed. The only
     * interaction allowed is {@link #missing()}, which always indicates nothing is missing.
     *
     * @param <D> dependency type
     * @param <B> {@link ResolvedSourceBuilder} type
     */
    @NonNullByDefault
    private static final class TerminalDependencies<D extends SourceDependency, B extends ResolvedSourceBuilder<?>>
            extends Dependencies<D, B> {
        private static final TerminalDependencies<?, ?> INSTANCE = new TerminalDependencies<>();

        private TerminalDependencies() {
            // hidden on purpose
        }

        @SuppressWarnings("unchecked")
        static <D extends SourceDependency, B extends ResolvedSourceBuilder<?>> TerminalDependencies<D, B> of() {
            return (TerminalDependencies<D, B>) INSTANCE;
        }

        @Override
        Set<D> missing() {
            return Set.of();
        }

        @Override
        Iterator<B> present() {
            return Collections.emptyIterator();
        }

        @Override
        void doResolveMissing(final D dependency, final B target) {
            throw uoe();
        }

        @Override
        <R> List<@NonNull R> doBuildResolved(final BiFunction<D, B, R> function) {
            throw uoe();
        }

        @Override
        public String toString() {
            return "TerminalDependencies{}";
        }

        private static UnsupportedOperationException uoe() {
            return new UnsupportedOperationException("should never be called");
        }
    }

    // the SourceInfoRef this object is attempting to resolve
    private final @NonNull R infoRef;

    // FIXME: Mutable state: we either have imports and includes, or product. Encapsulate the two possibilities into
    //        an internal class and eliminate TerminalDependencies.
    @NonNullByDefault
    private Dependencies<Import, ForModule> imports;
    @NonNullByDefault
    private Dependencies<Include, ForSubmodule> includes;
    private @Nullable ResolvedSourceInfo product;

    @NonNullByDefault
    private ResolvedSourceBuilder(final R infoRef) {
        this.infoRef = requireNonNull(infoRef);

        final var info = infoRef.info();
        imports = Dependencies.of(info.imports());
        includes = Dependencies.of(info.includes());
    }

    final @NonNull R infoRef() {
        return infoRef;
    }

    @NonNullByDefault
    final String humanName() {
        final var sourceId = sourceId();
        return humanName(sourceId.name(), sourceId.revision());
    }

    @NonNullByDefault
    static final String humanName(final Unqualified name, final @Nullable Revision revision) {
        final var localName = name.getLocalName();
        return revision == null ? localName : localName + "@" + revision;
    }

    final @NonNull Unqualified name() {
        return sourceId().name();
    }

    final @Nullable Revision revision() {
        return sourceId().revision();
    }

    final @NonNull SourceIdentifier sourceId() {
        return sourceInfo().sourceId();
    }

    abstract @NonNull SourceInfo sourceInfo();

    final @NonNull YangVersion yangVersion() {
        return sourceInfo().yangVersion();
    }

    /**
     * {@return {@code true} if all dependencies specified in {@link #sourceInfo()} have been satisfied}
     */
    boolean isResolved() {
        return product != null || missingImports().isEmpty() && missingIncludes().isEmpty();
    }

    /**
     * {@return the set of {@link Import}s that remain unresolved}
     */
    @NonNullByDefault
    final Set<Import> missingImports() {
        return imports.missing();
    }

    /**
     * {@return the set of {@link Include}s that remain unresolved}
     */
    @NonNullByDefault
    final Set<Include> missingIncludes() {
        return includes.missing();
    }

    /**
     * Adds a {@link ForModule} of an imported module.
     *
     * @param parentModule {@link ForModule} of the parentimodule
     * @param dependency the {@link Import} being satisfied
     * @param target {@link ForModule} of the imported module
     * @throws ReactorException if resolving the dependency to target would create a loop module import graph
     */
    @NonNullByDefault
    final void resolveImport(final ForModule parentModule, final Import dependency, final ForModule target)
            throws ReactorException {
        ensureBuilderOpened();

        // check that target module does not import parentModule
        // FIXME: 16.0.0: different exception for the case of self-import
        final var path = target.equals(requireNonNull(parentModule)) ? List.of(target)
            : importPathOf(target, parentModule);
        if (path != null) {
            final var sourceId = sourceId();
            final var depRef = dependency.sourceRef();
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                 new InferenceException(depRef != null ? depRef : sourceId.toReference(),
                     // FIXME: 16.0.0: humanName() and exact path
                     "Found circular dependency between modules %s and %s",
                     sourceId.name().getLocalName(), target.name().getLocalName()));
        }

        imports.resolveMissing(dependency, target);
    }

    /**
     * Check that specified module is not imported, directly or indirectly, by specified source.
     *
     * @param source the source
     * @param module the module
     * @return the reverse sequence of sources through which the specified module is imported, or {@code null} when it
     *         is not imported
     */
    private static @Nullable ArrayList<@NonNull ResolvedSourceBuilder<?>> importPathOf(
            final @NonNull ResolvedSourceBuilder<?> source, final @NonNull ForModule module) {
        final var impIt = source.imports.present();
        while (impIt.hasNext()) {
            final var target = impIt.next();
            if (target.equals(module)) {
                final var ret = new ArrayList<ResolvedSourceBuilder<?>>();
                ret.add(source);
                return ret;
            }
            final var path = importPathOf(target, module);
            if (path != null) {
                path.add(source);
                return path;
            }
        }
        final var incIt = source.includes.present();
        while (incIt.hasNext()) {
            final var path = importPathOf(incIt.next(), module);
            if (path != null) {
                path.add(source);
                return path;
            }
        }
        return null;
    }

    /**
     * Adds a {@link ForSubmodule} of an included submodule.
     *
     * @param dependency the {@link Include} dependency being satisfied
     * @param target {@link ForSubmodule} of the included submodule
     */
    @NonNullByDefault
    final void resolveInclude(final Include dependency, final ForSubmodule target) {
        ensureBuilderOpened();
        // FIXME: YANG 1 submodules should enforce no circular includes
        includes.resolveMissing(dependency, requireNonNull(target));
    }

    /**
     * Builds a finalized {@link ResolvedSourceInfo} using the map of already-resolved sources.
     *
     * @return ResolvedSourceInfo of this source
     */
    @NonNullByDefault
    final ResolvedSourceInfo build() {
        final var local = product;
        if (local != null) {
            return local;
        }
        final var result = buildProduct(
            imports.buildResolved((requirement, target) -> {
                final var source = target.infoRef();
                return new ResolvedImport(requirement, source.ref(), source.info().moduleName().getModule());
            }),
            includes.buildResolved((requirement, target) -> new ResolvedInclude(requirement, target.infoRef().ref())));
        product = result;
        imports = TerminalDependencies.of();
        includes = TerminalDependencies.of();
        return result;
    }

    @NonNullByDefault
    abstract ResolvedSourceInfo buildProduct(List<ResolvedImport> resolvedImports,
        List<ResolvedInclude> resolveIncludes);

    private void ensureBuilderOpened() {
        final var local = product;
        if (local != null) {
            throw new VerifyException("Attempted to modify " + this + " with product " + local);
        }
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("ref", infoRef).toString();
    }
}
