/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.RevisionUnion;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.DeclarationInSource;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

/**
 * A {@link SourceLinker} for a YANG {@code module}. It provides a meeting point for resolving
 * {@code include} statements to a consistent set of sources, such that violations of RFC6020/RFC7950 section 7.1.6
 * requirement that {@code Multiple revisions of the same submodule MUST NOT be included.} are reliably reported.
 */
@NonNullByDefault
final class ModuleLinker extends SourceLinker<SourceInfoRef.OfModule, ResolvedModuleInfo> implements ModuleBuilder {
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
        }

        /**
         * Exact revision inferred during resolution.
         */
        record Narrowed(StatementSourceReference sourceRef) implements Exactness {
            public Narrowed {
                requireNonNull(sourceRef);
            }

            @Override
            public String sourceString() {
                return "(narrowed) " + Exactness.super.sourceString();
            }
        }

        StatementSourceReference sourceRef();

        default String sourceString() {
            final var sourceRef = sourceRef();
            return switch (sourceRef) {
                case DeclarationInSource ref -> ref.toString();
                case StatementDeclaration ref -> "at " + ref;
                default -> "from " + sourceRef;
            };
        }
    }

    /**
     * The specification of how the source of a submodule should be looked up.
     */
    // TODO: consider promoting parts of this contract to model.spi.source.SourceDependency
    private sealed interface SubmoduleSpec {
        // nothing else
    }

    /**
     * A {@link SubmoduleSpec} lacking a revision specification, e.g. {@code include foo;}.
     */
    private record AnyRevision(StatementSourceReference sourceRef) implements SubmoduleSpec {
        AnyRevision {
            requireNonNull(sourceRef);
        }

        UnresolvedRevision narrowTo(final RevisionUnion revision) {
            return new UnresolvedRevision(revision, new Exactness.Narrowed(sourceRef));
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
     * An {@link ExactRevision} which has been resolved to a {@link SubmoduleLinker}.
     */
    private record ResolvedRevision(SubmoduleLinker submodule, Exactness exactness) implements ExactRevision {
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

        ResolvedRevision toResolved(final SubmoduleLinker submodule) {
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

    ModuleLinker(final SourceInfoRef.OfModule infoRef) throws ReactorException {
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
        final var spec = submoduleSpecs.get(requireNonNull(submodule));
        switch (spec) {
            case null -> throw new VerifyException("Attempted to narrow non-existing " + submodule.getLocalName());
            case AnyRevision any -> verify(submoduleSpecs.replace(submodule, any, any.narrowTo(revision)));
            default -> throw new VerifyException(
                "Attempted to narrow " + submodule.getLocalName() + " from " + spec);
        }
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
     * {@return the memoized {@link SubmoduleLinker}, or {@code null} if the submodule was not yet resolved}
     * @param submodule submodule name
     */
    @Nullable SubmoduleLinker lookupSubmodule(final Unqualified submodule) {
        return submoduleSpecs.get(requireNonNull(submodule)) instanceof ResolvedRevision resolved
            ? resolved.submodule : null;
    }

    /**
     * Resolve the requirement to include a particular submodule revision, reported by
     * {@link #lookupRevision(Unqualified)}, to a particular {@link SubmoduleLinker}.
     *
     * @param submodule the submodule
     */
    void resolveSubmodule(final SubmoduleLinker submodule) {
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
     * @param source the {@link SourceLinker} to the source of requirements
     * @throws ReactorException if a requirement conflicts with a previous requirement
     */
    void requireIncludes(final SourceLinker<?, ?> source) throws ReactorException {
        final var it = source.missingIncludes();
        while (it.hasNext()) {
            requireInclude(source, it.next());
        }
    }

    /**
     * Record a requirement for this module to {@code Include} a submodule.
     *
     * @param source the {@link SourceLinker} of requirements
     * @param dependency the {@link Include}
     * @throws ReactorException if the requirement conflicts with a previous requirement or cannot be added
     */
    private void requireInclude(final SourceLinker<?, ?> source, final Include dependency)
            throws ReactorException {
        final var name = dependency.name();
        final var revision = dependency.revision();
        if (revision == null) {
            // unspecified revision, never conflicts
            if (!submoduleSpecs.containsKey(name)) {
                checkInclude(source, dependency);
                final var depRef = dependency.sourceRef();
                submoduleSpecs.put(name, new AnyRevision(depRef != null ? depRef
                    : source.sourceId().toReference()));
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
    ResolvedModuleInfo doBuild(final List<ResolvedImport> resolvedImports,
            final List<ResolvedInclude> resolveIncludes) {
        return new ResolvedModuleInfo(infoRef(), resolvedImports, resolveIncludes);
    }

    /**
     * {@return the reference to the first include-without-revision of specified submodule or this module}
     * @param name submodule name
     */
    StatementSourceReference includeRefOf(final Unqualified name) {
        return submoduleSpecs.get(requireNonNull(name)) instanceof AnyRevision spec ? spec.sourceRef
            : sourceId().toReference();
    }

    /**
     * Check that a source can add an {@link Include} dependency to this module.
     *
     * @param source the source that is resolving the dependency
     * @param dependency the dependency being resolved
     * @throws ReactorException if the source cannot be add the dependency to this module
     */
    private void checkInclude(final SourceLinker<?, ?> source, final Include dependency) throws ReactorException {
        switch (source) {
            case ModuleLinker module -> verify(module == this);
            case SubmoduleLinker submodule -> {
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