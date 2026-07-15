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
import org.opendaylight.yangtools.yang.parser.spi.source.YangVersionLinkageException;

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
         * A {@link SubmoduleSpec} with a revision specification, e.g.
         * {@code include foo { revision-date 1970-01-01; }}.
         */
        // TODO: abstract sealed record when Java provides them as hinted at in
        //       https://youtu.be/BdLND9D81lI?si=tAX8gXsPC1FBh5tJ&t=1703
        private sealed interface ExactRevision extends SubmoduleSpec {

            Revision revision();

            StatementSourceReference sourceRef();
        }

        /**
         * An {@link ExactRevision} which has been resolved to a {@link ForSubmodule}.
         */
        private record ResolvedRevision(ForSubmodule submodule, StatementSourceReference sourceRef)
                implements ExactRevision {
            ResolvedRevision {
                requireNonNull(submodule);
                requireNonNull(sourceRef);
                revision();
            }

            @Override
            public Revision revision() {
                return verifyNotNull(submodule.sourceId().revision());
            }
        }

        /**
         * An {@link ExactRevision} which has not been resolved.
         */
        private record UnresolvedRevision(Revision revision, StatementSourceReference sourceRef)
                implements ExactRevision {
            UnresolvedRevision {
                requireNonNull(revision);
                requireNonNull(sourceRef);
            }

            ResolvedRevision toResolved(final ForSubmodule submodule) {
                if (!revision.equals(submodule.sourceId().revision())) {
                    throw new VerifyException("Attempted to resolve " + this + " with " + submodule.humanName());
                }
                return new ResolvedRevision(submodule, sourceRef);
            }
        }

        /**
         * The set of names of known submodules and their corresponding {@link SubmoduleSpec}.
         */
        private final LinkedHashMap<Unqualified, SubmoduleSpec> submoduleSpecs = new LinkedHashMap<>();

        // FIXME: hide this constructor
        @Deprecated
        ForModule(final SourceInfoRef.OfModule infoRef) {
            super(infoRef);
        }

        // FIXME: promote into public constructor when possible
        private void initSubmodules() throws ReactorException {
            requireIncludes(this);
        }

        /**
         * {@return the {@link Revision} of a submodule that is known to be required by this module, but has not been
         * resolved yet, or {@code null} if the submodule has been resolved or the revision is not yet known}
         * @param submodule submodule name
         */
        @Nullable Revision unresolvedRevisionOf(final Unqualified submodule) {
            return switch (submoduleSpecs.get(requireNonNull(submodule))) {
                case null -> throw new VerifyException("Unexpected submodule " + submodule.getLocalName());
                case UnresolvedRevision needed -> needed.revision;
                default -> null;
            };
        }

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

        @Nullable ForSubmodule lookupSubmodule(final Unqualified submodule) {
            return switch (submoduleSpecs.get(requireNonNull(submodule))) {
                case null -> throw new VerifyException("Unexpected submodule " + submodule.getLocalName());
                case ResolvedRevision resolved -> resolved.submodule;
                default -> null;
            };
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
                    source.beforeNewDependency(this, dependency);
                    submoduleSpecs.put(name, AnyRevision.INSTANCE);
                }
                return;
            }

            // FIXME: Java 25: merge the two cases below when we can say 'case AnyRevision _' and move this allocation
            //        there
            final var depRef = dependency.sourceRef();
            final var sourceRef = depRef != null ? depRef : source.sourceId().toReference();
            final var spec = new UnresolvedRevision(revision, sourceRef);

            // yes, we have a Map.get() + Map.put() and could be written as a Map.compute() operation, but this way is
            // actually more modern: we are using Java 21+ language features instead of Java 8+ java.util features
            // we do not care about the two HashMap lookup operations.
            switch (submoduleSpecs.get(name)) {
                case null -> {
                    source.beforeNewDependency(this, dependency);
                    submoduleSpecs.put(name, spec);
                }
                case AnyRevision any -> submoduleSpecs.put(name, spec);
                case ExactRevision exact -> {
                    final var exactRevision = exact.revision();
                    if (!revision.equals(exactRevision)) {
                        final var exactRef = exact.sourceRef();
                        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE,
                            source.sourceId(), new InferenceException(sourceRef, """
                                Cannot include-by-revision submodule %s in module %s: already included as %s %s""",
                                humanName(name, revision), humanName(), humanName(name, exactRevision),
                                switch (exactRef) {
                                    case DeclarationInSource ref -> ref.toString();
                                    case StatementDeclaration ref -> "at " + ref;
                                    default -> "from " + exactRef;
                                }));
                    }
                }
            }
        }

        @Override
        SourceInfo.Module sourceInfo() {
            return infoRef().info();
        }

        @Override
        void resolveBelongsTo(final BelongsTo dependency, final ForModule module) {
            throw new VerifyException("Attempted to resolve belongs-to in non-submodule" + this);
        }

        void checkInclude(final Include dependency, final ForSubmodule submodule) throws ReactorException {
            // As per https://www.rfc-editor.org/info/rfc7950/#section-12:
            //
            //      A YANG version 1.1 module MUST NOT include a YANG version 1
            //      submodule, and a YANG version 1 module MUST NOT include a YANG
            //      version 1.1 submodule.
            //
            final var moduleVersion = yangVersion();
            final var submoduleVersion = submodule.yangVersion();
            if (moduleVersion != submoduleVersion) {
                final var sourceId = sourceId();
                final var depRef = dependency.sourceRef();
                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                    new YangVersionLinkageException(depRef != null ? depRef : sourceId.toReference(),
                        "Cannot include a version %s submodule %s in a version %s module %s",
                        submoduleVersion, submodule.humanName(), moduleVersion, humanName()));
            }
        }

        @Override
        ResolvedSourceInfo.Module buildProduct(final List<ResolvedImport> resolvedImports,
                final List<ResolvedInclude> resolveIncludes) {
            return new ResolvedSourceInfo.Module(infoRef(), resolvedImports, resolveIncludes);
        }

        @Override
        void beforeNewDependency(final ForModule module, final Include dependency) {
            verify(module == this);
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
            if (!belongsTo.equals(module.sourceId().name())) {
                throw new VerifyException("Attempted to resolve belongs-to " + belongsTo.getLocalName()
                    + " with module " + module.humanName());
            }
            parentModule = module;
            module.requireIncludes(this);
        }

        @Override
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

        @Override
        void beforeNewDependency(final ForModule module, final Include dependency) throws ReactorException {
            if (module.yangVersion() != YangVersion.VERSION_1) {
                final var depRef = dependency.sourceRef();
                final var sourceId = sourceId();
                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                    new InferenceException(depRef != null ? depRef : sourceId.toReference(), """
                        Parent module %s does not include %s, YANG %s does not allow it to be included from submodule \
                        %s""", module.humanName(), dependency.name(), module.yangVersion(), humanName()));
            }
        }

        private void verifyNoParentModule(final ForModule module) {
            final var local = parentModule;
            if (local != null) {
                throw new VerifyException("Attempted to re-resolve belongs-to from " + local + " to " + module);
            }
        }
    }

    /**
     * A set of {@link SourceDependency} objects that need to be resolved to their corresponding {@link SourceInfoRef}.
     * This class is meant to track {@link Import} and {@link Include}, with {@link ForSubmodule} tracking
     * {@link BelongsTo} separately.
     *
     * <p>That separation allows us to assume {@code 0..N} cardinality and shared interpretation
     * of {@link SourceDependency#revision()}: it may or may not be a wildcard.
     *
     * @param <D> dependency type
     * @param <S> {@link SourceInfoRef} type
     */
    @NonNullByDefault
    private abstract static sealed class Dependencies<D extends SourceDependency, S extends SourceInfoRef> {
        /**
         * {@return an instance for the specified set of initial dependencies}
         * @param <D> dependency type
         * @param <S> {@link SourceInfoRef} type
         * @param dependencies the set of dependencies
         */
        static final <D extends SourceDependency, S extends SourceInfoRef> Dependencies<D, S> of(
                final Set<@NonNull D> dependencies) {
            return dependencies.isEmpty() ? NoDependencies.of() : new SomeDependencies<>(dependencies);
        }

        /**
         * {@return the unmodifiable view of dependencies that remain unresolved. Guaranteed to be updated by
         * {@link #resolveMissing(SourceDependency, SourceInfoRef)} invocations and iterators reporting
         * {@link ConcurrentModificationException}}
         */
        abstract Set<@NonNull D> missing();

        /**
         * Resolve a currently-missing dependency with a builder.
         *
         * @param dependency the dependency that is missing
         * @param infoRef the builder to use to resolve the dependency
         */
        final void resolveMissing(final @NonNull D dependency, final @NonNull S infoRef) {
            // split to keep argument checking consistent
            doResolveMissing(requireNonNull(dependency), requireNonNull(infoRef));
        }

        /**
         * Implementation of the {@link #resolveMissing(SourceDependency, SourceInfoRef)} contract. All arguments are
         * guaranteed to be non-{@code null} by the caller.
         *
         * @param dependency the dependency that is missing
         * @param infoRef the builder to use to resolve the dependency
         */
        abstract void doResolveMissing(@NonNull D dependency, @NonNull S infoRef);

        /**
         * Build a list of objects, each representing a dependency. Implementations of this method assert that all
         * dependencies have been satisfied and reports an exception if {@code #missing().isEmpty()} is known to be
         * {@code false}.
         *
         * @param <R> per-dependency result type
         * @param function the function to turn a dependency and its corresponding builder into the result type
         * @return a list of results
         */
        final <R> List<R> buildResolved(final BiFunction<D, S, R> function) {
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
        abstract <R> List<R> doBuildResolved(BiFunction<D, S, R> function);

        @Override
        public abstract String toString();
    }

    /**
     * An implementation of {@link Dependencies} indicating there are no dependencies.
     *
     * @param <D> dependency type
     * @param <S> {@link SourceInfoRef} type
     */
    @NonNullByDefault
    private static final class NoDependencies<D extends SourceDependency, S extends SourceInfoRef>
            extends Dependencies<D, S> {
        private static final NoDependencies<?, ?> INSTANCE = new NoDependencies<>();

        private NoDependencies() {
            // hidden on purpose
        }

        @SuppressWarnings("unchecked")
        static <D extends SourceDependency, S extends SourceInfoRef> NoDependencies<D, S> of() {
            return (NoDependencies<D, S>) INSTANCE;
        }

        @Override
        Set<D> missing() {
            return Set.of();
        }

        @Override
        void doResolveMissing(final D dependency, final S infoRef) {
            throw new VerifyException("Attempted to resolve unspecified " + dependency);
        }

        @Override
        <R> List<R> doBuildResolved(final BiFunction<D, S, R> function) {
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
     * @param <S> {@link SourceInfoRef} type
     */
    @NonNullByDefault
    private static final class SomeDependencies<D extends SourceDependency, S extends SourceInfoRef>
            extends Dependencies<D, S> {
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
        private final LinkedHashMap<D, @Nullable S> map;
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
        void doResolveMissing(final D dependency, final S infoRef) {
            if (!map.replace(dependency, null, infoRef)) {
                final var prev = map.get(dependency);
                throw prev == null
                    // replace failed because the dependency was not specified
                    ? new VerifyException("Attempted to resolve unspecified " + dependency)
                    // replace failed because the dependency was already resolved
                    : new VerifyException(
                        "Attempted to override resolution of " + dependency + " from " + prev + " to " + infoRef);
            }
        }

        @Override
        <R> List<R> doBuildResolved(final BiFunction<D, S, R> function) {
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
     * @param <S> {@link SourceInfoRef} type
     */
    @NonNullByDefault
    private static final class TerminalDependencies<D extends SourceDependency, S extends SourceInfoRef>
            extends Dependencies<D, S> {
        private static final TerminalDependencies<?, ?> INSTANCE = new TerminalDependencies<>();

        private TerminalDependencies() {
            // hidden on purpose
        }

        @SuppressWarnings("unchecked")
        static <D extends SourceDependency, S extends SourceInfoRef> TerminalDependencies<D, S> of() {
            return (TerminalDependencies<D, S>) INSTANCE;
        }

        @Override
        Set<D> missing() {
            return Set.of();
        }

        @Override
        void doResolveMissing(final D dependency, final S infoRef) {
            throw uoe();
        }

        @Override
        <R> List<@NonNull R> doBuildResolved(final BiFunction<D, S, R> function) {
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
    private Dependencies<Import, SourceInfoRef.OfModule> imports;
    @NonNullByDefault
    private Dependencies<Include, SourceInfoRef.OfSubmodule> includes;
    private @Nullable ResolvedSourceInfo product;

    @NonNullByDefault
    private ResolvedSourceBuilder(final R infoRef) {
        this.infoRef = requireNonNull(infoRef);

        final var info = infoRef.info();
        imports = Dependencies.of(info.imports());
        includes = Dependencies.of(info.includes());
    }

    @NonNullByDefault
    static final ForModule forModule(final SourceInfoRef.OfModule infoRef) throws ReactorException {
        final var ret = new ForModule(infoRef);
        ret.initSubmodules();
        return ret;
    }

    final @NonNull R infoRef() {
        return infoRef;
    }

    final @NonNull SourceIdentifier sourceId() {
        return sourceInfo().sourceId();
    }

    abstract @NonNull SourceInfo sourceInfo();

    final @NonNull YangVersion yangVersion() {
        return sourceInfo().yangVersion();
    }

    @NonNullByDefault
    final String humanName() {
        final var sourceId = sourceId();
        return humanName(sourceId.name(), sourceId.revision());
    }

    @NonNullByDefault
    static final String humanName(final Unqualified name, final @Nullable Revision revision) {
        var ret = name.getLocalName();
        if (revision != null) {
            ret = ret + "@" + revision;
        }
        return ret;
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
     * Adds a {@link SourceInfoRef} of an imported module.
     *
     * @param dependency the {@link Import} being satisfied
     * @param link {@link SourceInfoRef} of the imported module.
     */
    @NonNullByDefault
    final void resolveImport(final Import dependency, final SourceInfoRef link) {
        if (!(link instanceof SourceInfoRef.OfModule module)) {
            throw new VerifyException(
                "Attempted to resolve import " + dependency + " with non-module " + link);
        }
        ensureBuilderOpened();
        imports.resolveMissing(dependency, module);
    }

    /**
     * Adds a {@link SourceInfoRef} of an included submodule.
     *
     * @param dependency the {@link Include} dependency being satisfied
     * @param link {@link SourceInfoRef} of the included submodule.
     */
    @NonNullByDefault
    final void resolveInclude(final Include dependency, final SourceInfoRef link) {
        if (!(link instanceof SourceInfoRef.OfSubmodule submodule)) {
            throw new VerifyException(
                "Attempted to resolve include " + dependency + " with non-submodule " + link);
        }
        ensureBuilderOpened();
        includes.resolveMissing(dependency, submodule);
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of the parent module this submodule belongs to.
     *
     * @param dependency the {@link BelongsTo} being satistifed
     * @param link {@link ResolvedSourceBuilder} of the parent module.
     */
    @NonNullByDefault
    final void resolveBelongsTo(final BelongsTo dependency, final ResolvedSourceBuilder<?> link) {
        if (!(link instanceof ForModule module)) {
            throw new VerifyException(
                "Attempted to resolve belongs-to " + dependency + " with non-module " + link);
        }
        ensureBuilderOpened();
        resolveBelongsTo(dependency, module);
    }

    @NonNullByDefault
    abstract void resolveBelongsTo(BelongsTo dependency, ForModule module);

    @NonNullByDefault
    abstract void beforeNewDependency(ForModule module, Include dependency) throws ReactorException;

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
            imports.buildResolved((requirement, link) ->
                new ResolvedImport(requirement, link.ref(), link.info().moduleName().getModule())),
            includes.buildResolved((requirement, link) -> new ResolvedInclude(requirement, link.ref())));
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
