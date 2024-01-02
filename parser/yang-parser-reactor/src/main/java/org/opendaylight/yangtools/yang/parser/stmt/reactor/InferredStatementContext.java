/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.OnDemandSchemaTreeStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A statement which has been inferred to exist. Functionally it is equivalent to a SubstatementContext, but it is not
 * backed by a declaration (and declared statements). It is backed by a prototype StatementContextBase and has only
 * effective substatements, which are either transformed from that prototype or added by inference.
 */
final class InferredStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> implements OnDemandSchemaTreeStorage {
    // An effective copy view, with enough information to decide what to do next
    private static final class EffectiveCopy implements Immutable {
        // Original statement
        private final ReactorStmtCtx<?, ?, ?> orig;
        // Effective view, if the statement is to be reused it equals to orig
        private final ReactorStmtCtx<?, ?, ?> copy;

        EffectiveCopy(final ReactorStmtCtx<?, ?, ?> orig, final ReactorStmtCtx<?, ?, ?> copy) {
            this.orig = requireNonNull(orig);
            this.copy = requireNonNull(copy);
        }

        boolean isReused() {
            return orig == copy;
        }

        ReactorStmtCtx<?, ?, ?> toChildContext(final @NonNull InferredStatementContext<?, ?, ?> parent) {
            return isReused() ? orig.replicaAsChildOf(parent) : copy;
        }

        ReactorStmtCtx<?, ?, ?> toReusedChild(final @NonNull InferredStatementContext<?, ?, ?> parent) {
            verify(isReused(), "Attempted to discard copy %s", copy);
            return orig.replicaAsChildOf(parent);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(InferredStatementContext.class);

    // Sentinel objects for 'substatements', String is a good enough type
    private static final @NonNull String REUSED_SUBSTATEMENTS = "reused";
    private static final @NonNull String SWEPT_SUBSTATEMENTS = "swept";

    private final @NonNull StatementContextBase<A, D, E> prototype;
    private final @NonNull StatementContextBase<?, ?, ?> parent;
    private final @NonNull ReactorStmtCtx<A, D, E> originalCtx;
    private final QNameModule targetModule;
    private final A argument;

    // Indicates whether or not this statement's substatement file was modified, i.e. it is not quite the same as the
    // prototype's file.
    private boolean modified;

    /**
     * Effective substatements, lazily materialized. This field can have four states:
     * <ul>
     *   <li>it can be {@code null}, in which case no materialization has taken place</li>
     *   <li>it can be a {@link HashMap}, in which case partial materialization has taken place</li>
     *   <li>it can be a {@link List}, in which case full materialization has taken place</li>
     *   <li>it can be {@link SWEPT_SUBSTATEMENTS}, in which case materialized state is no longer available</li>
     * </ul>
     */
    private Object substatements;

    private InferredStatementContext(final InferredStatementContext<A, D, E> original,
            final StatementContextBase<?, ?, ?> parent) {
        super(original);
        this.parent = requireNonNull(parent);
        targetModule = original.targetModule;
        prototype = original.prototype;
        originalCtx = original.originalCtx;
        argument = original.argument;
        modified = original.modified;
        // Substatements are initialized here
        substatements = ImmutableList.of();
    }

    InferredStatementContext(final StatementContextBase<?, ?, ?> parent, final StatementContextBase<A, D, E> prototype,
            final CopyType myCopyType, final CopyType childCopyType, final QNameModule targetModule) {
        super(prototype, myCopyType, childCopyType);
        this.parent = requireNonNull(parent);
        this.prototype = requireNonNull(prototype);
        argument = targetModule == null ? prototype.argument()
                : prototype.definition().adaptArgumentValue(prototype, targetModule);
        this.targetModule = targetModule;

        final var origCtx = prototype.getOriginalCtx().orElse(prototype);
        verify(origCtx instanceof ReactorStmtCtx, "Unexpected original %s", origCtx);
        originalCtx = (ReactorStmtCtx<A, D, E>) origCtx;

        // Mark prototype as blocking statement cleanup
        prototype.incRef();
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return ImmutableList.of();
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        return mutableEffectiveSubstatements(ensureEffectiveSubstatements());
    }

    @Override
    public Iterable<? extends @NonNull StmtContext<?, ?, ?>> allSubstatements() {
        // No need to concat with declared
        return effectiveSubstatements();
    }

    @Override
    public Stream<? extends @NonNull StmtContext<?, ?, ?>> allSubstatementsStream() {
        // No need to concat with declared
        return effectiveSubstatements().stream();
    }

    @Override
    public StatementSourceReference sourceReference() {
        return originalCtx.sourceReference();
    }

    @Override
    public String rawArgument() {
        return originalCtx.rawArgument();
    }

    @Override
    public Optional<StmtContext<A, D, E>> getOriginalCtx() {
        return Optional.of(originalCtx);
    }

    @Override
    public Optional<StmtContext<A, D, E>> getPreviousCopyCtx() {
        return Optional.of(prototype);
    }

    @Override
    public D declared() {
        /*
         * Share original instance of declared statement between all effective statements which have been copied or
         * derived from this original declared statement.
         */
        return originalCtx.declared();
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef) {
        substatements = removeStatementFromEffectiveSubstatements(ensureEffectiveSubstatements(), statementDef);
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef,
            final String statementArg) {
        substatements = removeStatementFromEffectiveSubstatements(ensureEffectiveSubstatements(), statementDef,
            statementArg);
    }

    @Override
    public void addEffectiveSubstatement(final Mutable<?, ?, ?> substatement) {
        substatements = addEffectiveSubstatement(ensureEffectiveSubstatements(), substatement);
        afterAddEffectiveSubstatement(substatement);
    }

    @Override
    void addEffectiveSubstatementsImpl(final Collection<? extends Mutable<?, ?, ?>> statements) {
        substatements = addEffectiveSubstatementsImpl(ensureEffectiveSubstatements(), statements);
    }

    @Override
    InferredStatementContext<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        return new InferredStatementContext<>(this, newParent);
    }

    @Override
    E createEffective(final StatementFactory<A, D, E> factory) {
        // If we have not materialized we do not have a difference in effective substatements, hence we can forward
        // towards the source of the statement.
        accessSubstatements();
        return substatements == null ? tryToReusePrototype(factory) : createInferredEffective(factory);
    }

    private @NonNull E createInferredEffective(final @NonNull StatementFactory<A, D, E> factory) {
        return createInferredEffective(factory, this, streamDeclared(), streamEffective());
    }

    @Override
    E createInferredEffective(final StatementFactory<A, D, E> factory, final InferredStatementContext<A, D, E> ctx,
            final Stream<? extends ReactorStmtCtx<?, ?, ?>> declared,
            final Stream<? extends ReactorStmtCtx<?, ?, ?>> effective) {
        return originalCtx.createInferredEffective(factory, ctx, declared, effective);
    }

    private @NonNull E tryToReusePrototype(final @NonNull StatementFactory<A, D, E> factory) {
        final var origEffective = prototype.buildEffective();
        final var origSubstatements = origEffective.effectiveSubstatements();

        // First check if we can reuse the entire prototype
        if (!factory.canReuseCurrent(this, prototype, origSubstatements)) {
            return internAlongCopyAxis(factory, tryToReuseSubstatements(factory, origEffective));
        }

        // We can reuse this statement let's see if all statements agree...
        // ... no substatements to deal with, we can freely reuse the original
        if (origSubstatements.isEmpty()) {
            LOG.debug("Reusing empty: {}", origEffective);
            substatements = ImmutableList.of();
            prototype.decRef();
            return origEffective;
        }

        // ... all are context independent, reuse the original
        if (allSubstatementsContextIndependent()) {
            LOG.debug("Reusing context-independent: {}", origEffective);
            substatements = noRefs() ? REUSED_SUBSTATEMENTS : reusePrototypeReplicas();
            prototype.decRef();
            return origEffective;
        }

        // ... copy-sensitive check
        final var declCopy = effectiveCopy(prototype.streamDeclared());
        final var effCopy = effectiveCopy(prototype.streamEffective());

        // ... are any copy-sensitive?
        if (allReused(declCopy) && allReused(effCopy)) {
            LOG.debug("Reusing after substatement check: {}", origEffective);
            substatements = noRefs() ? REUSED_SUBSTATEMENTS
                : reusePrototypeReplicas(Streams.concat(declCopy.stream(), effCopy.stream())
                    .map(copy -> copy.toReusedChild(this)));
            prototype.decRef();
            return origEffective;
        }

        // *sigh*, ok, heavy lifting through a shallow copy
        final var declared = adoptSubstatements(declCopy);
        final var effective = adoptSubstatements(effCopy);
        substatements = declared.isEmpty() ? effective
            : Streams.concat(declared.stream(), effective.stream()).collect(ImmutableList.toImmutableList());
        prototype.decRef();

        // Values are the effective copies, hence this efficiently deals with recursion.
        return internAlongCopyAxis(factory,
            originalCtx.createInferredEffective(factory, this, declared.stream(), effective.stream()));
    }

    private @NonNull E tryToReuseSubstatements(final @NonNull StatementFactory<A, D, E> factory,
            final @NonNull E original) {
        if (allSubstatementsContextIndependent()) {
            LOG.debug("Reusing substatements of: {}", prototype);
            substatements = noRefs() ? REUSED_SUBSTATEMENTS : reusePrototypeReplicas();
            prototype.decRef();
            return factory.copyEffective(this, original);
        }

        // Fall back to full instantiation, which populates our substatements. Then check if we should be reusing
        // the substatement list, as this operation turned out to not affect them.
        final E effective = createInferredEffective(factory);
        // Since we have forced instantiation to deal with this case, we also need to reset the 'modified' flag
        modified = false;

        if (sameSubstatements(original.effectiveSubstatements(), effective)) {
            LOG.debug("Reusing unchanged substatements of: {}", prototype);
            return factory.copyEffective(this, original);
        }
        return effective;
    }

    private @NonNull E internAlongCopyAxis(final StatementFactory<A, D, E> factory, final @NonNull E stmt) {
        if (!modified) {
            final EffectiveStatementState state = factory.extractEffectiveState(stmt);
            if (state != null) {
                return prototype.unmodifiedEffectiveSource().attachEffectiveCopy(state, stmt);
            }
        }
        return stmt;
    }

    private List<ReactorStmtCtx<?, ?, ?>> reusePrototypeReplicas() {
        return reusePrototypeReplicas(Streams.concat(prototype.streamDeclared(), prototype.streamEffective()));
    }

    private List<ReactorStmtCtx<?, ?, ?>> reusePrototypeReplicas(final Stream<ReactorStmtCtx<?, ?, ?>> stream) {
        return stream
            .map(stmt -> {
                final var ret = stmt.replicaAsChildOf(this);
                ret.buildEffective();
                return ret;
            })
            .collect(Collectors.toUnmodifiableList());
    }

    private static boolean sameSubstatements(final Collection<?> original, final EffectiveStatement<?, ?> effective) {
        final var copied = effective.effectiveSubstatements();
        if (copied != effective.effectiveSubstatements() || original.size() != copied.size()) {
            // Do not bother if result is treating substatements as transient
            return false;
        }

        final var cit = copied.iterator();
        for (var origChild : original) {
            verify(cit.hasNext());
            // Identity comparison on purpose to side-step whatever equality there might be. We want to reuse instances
            // after all.
            if (origChild != cit.next()) {
                return false;
            }
        }
        verify(!cit.hasNext());
        return true;
    }

    private static boolean allReused(final List<EffectiveCopy> entries) {
        return entries.stream().allMatch(EffectiveCopy::isReused);
    }

    @Override
    ReactorStmtCtx<A, D, E> unmodifiedEffectiveSource() {
        return modified ? this : prototype.unmodifiedEffectiveSource();
    }

    @Override
    boolean hasEmptySubstatements() {
        return substatements == null ? prototype.hasEmptySubstatements()
            // Note: partial instantiation, as indicated by HashMap is always non-empty
            : substatements instanceof List<?> list && list.isEmpty();
    }

    @Override
    boolean noSensitiveSubstatements() {
        accessSubstatements();
        if (substatements == null) {
            // No difference, defer to prototype
            return prototype.allSubstatementsContextIndependent();
        }
        if (substatements instanceof List) {
            // Fully materialized, walk all statements
            return noSensitiveSubstatements(castEffective(substatements));
        }

        // Partially-materialized. This case has three distinct outcomes:
        // - prototype does not have a sensitive statement (1)
        // - protype has a sensitive substatement, and
        //   - we have not marked is as unsupported (2)
        //   - we have marked it as unsupported (3)
        //
        // Determining the outcome between (2) and (3) is a bother, this check errs on the side of false negative side
        // and treats (3) as (2) -- i.e. even if we marked a sensitive statement as unsupported, we still consider it
        // as affecting the result.
        return prototype.allSubstatementsContextIndependent()
            && noSensitiveSubstatements(castMaterialized(substatements).values());
    }

    @Override
    <X, Z extends EffectiveStatement<X, ?>> @NonNull Optional<X> findSubstatementArgumentImpl(
            final @NonNull Class<Z> type) {
        if (substatements instanceof List) {
            return super.findSubstatementArgumentImpl(type);
        }

        final Optional<X> templateArg = prototype.findSubstatementArgument(type);
        if (templateArg.isEmpty()) {
            return templateArg;
        }
        if (SchemaTreeEffectiveStatement.class.isAssignableFrom(type)) {
            // X is known to be QName
            return (Optional<X>) templateArg.map(template -> ((QName) template).bindTo(targetModule));
        }
        return templateArg;
    }

    @Override
    boolean hasSubstatementImpl(final @NonNull Class<? extends EffectiveStatement<?, ?>> type) {
        return substatements instanceof List ? super.hasSubstatementImpl(type)
            // We do not allow deletion of partially-materialized statements, hence this is accurate
            : prototype.hasSubstatement(type);
    }

    @Override
    public <Y extends DeclaredStatement<QName>, Z extends SchemaTreeEffectiveStatement<Y>>
            StmtContext<QName, Y, Z> requestSchemaTreeChild(final QName qname) {
        if (substatements instanceof List) {
            // We have performed materialization, hence we have triggered creation of all our schema tree child
            // statements.
            return null;
        }

        // Determine if the requested QName can be satisfied from the prototype: for that to happen it has to match
        // our transformation implied by targetModule.
        final var requestedNamespace = qname.getModule();
        final QName templateQName;
        if (targetModule != null) {
            if (!targetModule.equals(requestedNamespace)) {
                return null;
            }
            templateQName = qname.bindTo(StmtContextUtils.getModuleQName(prototype));
        } else {
            if (!StmtContextUtils.getModuleQName(prototype).equals(requestedNamespace)) {
                return null;
            }
            templateQName = qname;
        }

        LOG.debug("Materializing child {} from {}", qname, templateQName);

        final StmtContext<?, ?, ?> template;
        if (prototype instanceof InferredStatementContext<?, ?, ?> inferredPrototype) {
            // Note: we need to access namespace here, as the target statement may have already been populated, in which
            //       case we want to obtain the statement in local namespace storage.
            template = inferredPrototype.namespaceItem(ParserNamespaces.schemaTree(), templateQName);
        } else {
            template = prototype.allSubstatementsStream()
                .filter(stmt -> stmt.producesEffective(SchemaTreeEffectiveStatement.class)
                    && templateQName.equals(stmt.argument()))
                .findAny()
                .orElse(null);
        }

        if (template == null) {
            // We do not have a template, this child does not exist. It may be added later, but that is someone else's
            // responsibility.
            LOG.debug("Child {} does not have a template", qname);
            return null;
        }

        @SuppressWarnings("unchecked")
        final var ret = (Mutable<QName, Y, Z>) copySubstatement(template).orElseThrow(
            () -> new InferenceException(this, "Failed to materialize child %s template %s", qname, template));

        // Careful here: first add the substatement and only complete it afterwards
        final var toAdd = verifyStatement(ret);
        addMaterialized(template, toAdd);
        ensureCompletedExecution(toAdd);

        LOG.debug("Child {} materialized", qname);
        return ret;
    }

    // Instantiate this statement's effective substatements. Note this method has side-effects in namespaces and overall
    // BuildGlobalContext, hence it must be called at most once.
    private List<ReactorStmtCtx<?, ?, ?>> ensureEffectiveSubstatements() {
        accessSubstatements();
        return substatements instanceof List ? castEffective(substatements)
            // We have either not started or have only partially-materialized statements, ensure full materialization
            : initializeSubstatements();
    }

    @Override
    Iterator<ReactorStmtCtx<?, ?, ?>> effectiveChildrenToComplete() {
        // When we have not initialized, there are no statements to catch up: we will catch up when we are copying
        // from prototype (which is already at ModelProcessingPhase.EFFECTIVE_MODEL).
        if (substatements == null) {
            return Collections.emptyIterator();
        }
        accessSubstatements();
        if (substatements instanceof HashMap) {
            return castMaterialized(substatements).values().iterator();
        } else {
            return castEffective(substatements).iterator();
        }
    }

    @Override
    Stream<? extends @NonNull ReactorStmtCtx<?, ?, ?>> streamDeclared() {
        return Stream.empty();
    }

    @Override
    Stream<? extends @NonNull ReactorStmtCtx<?, ?, ?>> streamEffective() {
        return ensureEffectiveSubstatements().stream().filter(StmtContext::isSupportedToBuildEffective);
    }

    private void accessSubstatements() {
        if (substatements instanceof String) {
            throw new VerifyException("Access to " + substatements + " substatements of " + this);
        }
    }

    @Override
    void markNoParentRef() {
        final Object local = substatements;
        if (local != null) {
            markNoParentRef(castEffective(local));
        }
    }

    @Override
    int sweepSubstatements() {
        final Object local = substatements;
        substatements = SWEPT_SUBSTATEMENTS;
        int count = 0;
        if (local instanceof List) {
            final List<ReactorStmtCtx<?, ?, ?>> list = castEffective(local);
            sweep(list);
            count = countUnswept(list);
        }
        return count;
    }

    private List<ReactorStmtCtx<?, ?, ?>> initializeSubstatements() {
        final var declared = prototype.mutableDeclaredSubstatements();
        final var effective = prototype.mutableEffectiveSubstatements();

        // We are about to instantiate some substatements. The simple act of materializing them may end up triggering
        // namespace lookups, which in turn can materialize copies by themselves, running ahead of our materialization.
        // We therefore need a meeting place for, which are the partially-materialized substatements. If we do not have
        // them yet, instantiate them and we need to populate them as well.
        final int expectedSize = declared.size() + effective.size();
        var materializedSchemaTree = castMaterialized(substatements);
        if (materializedSchemaTree == null) {
            substatements = materializedSchemaTree = Maps.newHashMapWithExpectedSize(expectedSize);
        }

        final var buffer = new ArrayList<ReactorStmtCtx<?, ?, ?>>(expectedSize);
        for (var stmtContext : declared) {
            if (stmtContext.isSupportedByFeatures()) {
                copySubstatement(stmtContext, buffer, materializedSchemaTree);
            }
        }
        for (var stmtContext : effective) {
            copySubstatement(stmtContext, buffer, materializedSchemaTree);
        }

        final var ret = beforeAddEffectiveStatementUnsafe(ImmutableList.of(), buffer.size());
        ret.addAll(buffer);
        substatements = ret;
        modified = true;

        prototype.decRef();
        return ret;
    }

    //
    // Statement copy mess starts here. As it turns out, it's not that much of a mess, but it does make your head spin
    // sometimes. Tread softly because you tread on my dreams.
    //

    private ImmutableList<ReactorStmtCtx<?, ?, ?>> adoptSubstatements(final List<EffectiveCopy> list) {
        return list.stream()
            .map(copy -> copy.toChildContext(this))
            .collect(ImmutableList.toImmutableList());
    }

    private List<EffectiveCopy> effectiveCopy(final Stream<? extends ReactorStmtCtx<?, ?, ?>> stream) {
        return stream
            .map(this::effectiveCopy)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Create an effective copy of a prototype's substatement as a child of this statement. This is a bit tricky, as
     * we are called from {@link #tryToReusePrototype(StatementFactory)} and we are creating copies of prototype
     * statements -- which triggers {@link StatementSupport#onStatementAdded(Mutable)}, which in turn can loop around
     * to {@link #requestSchemaTreeChild(QName)} -- which creates the statement and hence we can end up performing two
     * copies.
     *
     * @param template Prototype substatement
     * @return An {@link EffectiveCopy}, or {@code null} if not applicable
     */
    private @Nullable EffectiveCopy effectiveCopy(final ReactorStmtCtx<?, ?, ?> template) {
        if (substatements instanceof HashMap) {
            // we have partial materialization by requestSchemaTreeChild() after we started tryToReusePrototype(), check
            // if the statement has already been copied -- we need to pick it up in that case.
            final var copy = castMaterialized(substatements).get(template);
            if (copy != null) {
                return new EffectiveCopy(template, copy);
            }
        }

        final var copy = template.asEffectiveChildOf(this, childCopyType(), targetModule);
        return copy == null ? null : new EffectiveCopy(template, copy);
    }

    private void copySubstatement(final Mutable<?, ?, ?> substatement, final Collection<ReactorStmtCtx<?, ?, ?>> buffer,
            final Map<StmtContext<?, ?, ?>, ReactorStmtCtx<?, ?, ?>> materializedSchemaTree) {
        // Consult materialized substatements. We are in a copy operation and will end up throwing materialized
        // statements away -- hence we do not perform Map.remove() to save ourselves a mutation operation.
        //
        // We could also perform a Map.containsKey() and perform a bulk add, but that would mean the statement order
        // against parent would change -- and we certainly do not want that to happen.
        final var materialized = findMaterialized(materializedSchemaTree, substatement);
        if (materialized == null) {
            copySubstatement(substatement).ifPresent(copy -> {
                // Careful here: first add the substatement and only complete it afterwards
                final var cast = verifyStatement(copy);
                materializedSchemaTree.put(substatement, cast);
                ensureCompletedExecution(cast);
                buffer.add(cast);
            });
        } else {
            buffer.add(materialized);
        }
    }

    private <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Optional<Mutable<X, Y, Z>>
            copySubstatement(final StmtContext<X, Y, Z> substatement) {
        return substatement.copyAsChildOf(this, childCopyType(), targetModule);
    }

    private void addMaterialized(final StmtContext<?, ?, ?> template, final ReactorStmtCtx<?, ?, ?> copy) {
        final HashMap<StmtContext<?, ?, ?>, ReactorStmtCtx<?, ?, ?>> materializedSchemaTree;
        if (substatements == null) {
            // Lazy initialization of backing map. We do not expect this to be used often or multiple times -- each hit
            // here means an inference along schema tree, such as deviate/augment. HashMap requires power-of-two and
            // defaults to 0.75 load factor -- we therefore size it to 4, i.e. next two inserts will not cause a
            // resizing operation.
            materializedSchemaTree = new HashMap<>(4);
            substatements = materializedSchemaTree;
            modified = true;
        } else {
            verify(substatements instanceof HashMap, "Unexpected substatements %s", substatements);
            materializedSchemaTree = castMaterialized(substatements);
        }

        final var existing = materializedSchemaTree.put(template, copy);
        if (existing != null) {
            throw new VerifyException(
                "Unexpected duplicate request for " + copy.argument() + " previous result was " + existing);
        }
    }

    private static @Nullable ReactorStmtCtx<?, ?, ?> findMaterialized(
            final Map<StmtContext<?, ?, ?>, ReactorStmtCtx<?, ?, ?>> materializedSchemaTree,
            final StmtContext<?, ?, ?> template) {
        return materializedSchemaTree == null ? null : materializedSchemaTree.get(template);
    }

    @SuppressWarnings("unchecked")
    private static List<ReactorStmtCtx<?, ?, ?>> castEffective(final Object substatements) {
        return (List<ReactorStmtCtx<?, ?, ?>>) substatements;
    }

    @SuppressWarnings("unchecked")
    private static HashMap<StmtContext<?, ?, ?>, ReactorStmtCtx<?, ?, ?>> castMaterialized(final Object substatements) {
        return (HashMap<StmtContext<?, ?, ?>, ReactorStmtCtx<?, ?, ?>>) substatements;
    }

    // Statement copy mess ends here

    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in SubstatementContext. If any adjustment is made here, make sure it is
     * properly updated there.
     */
    @Override
    public A argument() {
        return argument;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return parent;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentStorage() {
        return parent;
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

    @Override
    public EffectiveConfig effectiveConfig() {
        return effectiveConfig(parent);
    }

    @Override
    protected boolean isIgnoringIfFeatures() {
        return isIgnoringIfFeatures(parent);
    }

    @Override
    protected boolean isIgnoringConfig() {
        return isIgnoringConfig(parent);
    }

    @Override
    public boolean isSupportedToBuildEffective() {
        // Our prototype may have fizzled, for example due to it being a implicit statement guarded by if-feature which
        // evaluates to false. If that happens, this statement also needs to report unsupported -- and we want to cache
        // that information for future reuse.
        boolean ret = super.isSupportedToBuildEffective();
        if (ret && !prototype.isSupportedToBuildEffective()) {
            setUnsupported();
            ret = false;
        }
        return ret;
    }

    @Override
    boolean computeSupportedByFeatures() {
        return prototype.isSupportedByFeatures();
    }

    @Override
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }
}
