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
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.OnDemandSchemaTreeStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextDefaults;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A statement which has been inferred to exist. Functionally it is equivalent to a SubstatementContext, but it is not
 * backed by a declaration (and declared statements). It is backed by a prototype StatementContextBase and has only
 * effective substatements, which are either transformed from that prototype or added by inference.
 */
final class InferredStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> implements OnDemandSchemaTreeStorageNode {
    // Base interface
    private abstract static class Substatements {

        abstract @NonNull List<StatementContextBase<?, ?, ?>> materialize();
    }

    // Do not allow access to substatements
    private static final class Consumed extends Substatements {
        private final @NonNull List<StatementContextBase<?, ?, ?>> materialized;

        Consumed(final List<StatementContextBase<?, ?, ?>> materialized) {
           this.materialized = requireNonNull(materialized);
        }

        @Override
        List<StatementContextBase<?, ?, ?>> materialize() {
            return materialized;
        }
    }

    // An evolving promise to have some substatements
    private final class Promised extends Substatements {
        private Substatements upstream;
        private Object materialized;

        private Promised(final Object materialized, final Substatements upstream) {
            this.upstream = upstream;
            this.materialized = materialized;
        }

        Promised() {
            this(null, null);
        }

        Promised(final Object materialized) {
            this(requireNonNull(materialized), null);
        }

        Promised(final Substatements upstream) {
            this(null, requireNonNull(upstream));
        }

        // Instantiate this statement's effective substatements. Note this method has side-effects in namespaces and
        // overall BuildGlobalContext, hence it must be done once.
        @Override
        @NonNull List<StatementContextBase<?, ?, ?>> materialize() {
            if (materialized instanceof List) {
                return castEffective(materialized);
            } if (materialized instanceof HashMap || materialized == null) {
                final var list = createSubstatements(upstream, castMaterialized(materialized));
                upstream = null;
                materialized = list;
                return list;
            } else {
                throw new VerifyException("Unhandled state " + materialized);
            }
        }

        void update(final List<StatementContextBase<?, ?, ?>> newMaterialized) {
            verify(materialized instanceof List, "Attempted to update non-full %s", materialized);
            materialized = requireNonNull(newMaterialized);
        }

        void addMaterialized(final StmtContext<?, ?, ?> template, final Mutable<?, ?, ?> copy) {
            final Map<StmtContext<?, ?, ?>, StatementContextBase<?, ?, ?>> partial;
            if (materialized == null) {
                // Lazy initialization of backing map. We do not expect this to be used often or multiple times -- each
                // hit here means an inference along schema tree, such as deviate/augment. HashMap requires power-of-two
                // and defaults to 0.75 load factor -- we therefore size it to 4, i.e. next two inserts will not cause a
                // resizing operation.
                materialized = partial = new HashMap<>(4);
            } else {
                verify(materialized instanceof HashMap, "Unexpected state " + materialized);
                partial = castMaterialized(materialized);
            }

            final StmtContext<?, ?, ?> existing = partial.put(template, (StatementContextBase<?, ?, ?>) copy);
            if (existing != null) {
                throw new VerifyException("Unexpected duplicate request for " + copy.getStatementArgument()
                    + " previous result was " + existing);
            }
        }

        Collection<StatementContextBase<?, ?, ?>> materialized() {
            if (materialized == null) {
                return ImmutableList.of();
            } else if (materialized instanceof HashMap) {
                return castMaterialized(materialized).values();
            } else {
                return castEffective(materialized);
            }
        }

        boolean fullyMaterialized() {
            return materialized instanceof List;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(InferredStatementContext.class);

    private final @NonNull StatementContextBase<A, D, E> prototype;
    private final @NonNull StatementContextBase<?, ?, ?> parent;
    private final @NonNull StmtContext<A, D, E> originalCtx;
    private final @NonNull CopyType childCopyType;
    private final QNameModule targetModule;
    private final A argument;

    /**
     * Effective substatements, lazily materialized. This field can have four states:
     * <ul>
     *   <li>it can be {@code null}, in which case no materialization has taken place</li>
     *   <li>it can be a {@link HashMap}, in which case partial materialization has taken place</li>
     *   <li>it can be a {@link Materialized}, in which case full materialization has taken place</li>
     *   <li>it can be {@link Consumed}, in which case the statement has been built and we do not allow
     *       access to its substatements</li>
     * </ul>
     */
    private Substatements substatements;

    private InferredStatementContext(final InferredStatementContext<A, D, E> original,
            final StatementContextBase<?, ?, ?> parent) {
        super(original);
        this.parent = requireNonNull(parent);
        this.childCopyType = original.childCopyType;
        this.targetModule = original.targetModule;
        this.prototype = original.prototype;
        this.originalCtx = original.originalCtx;
        this.argument = original.argument;
        // Substatements are initialized here
        this.substatements = new Promised(ImmutableList.of());
    }

    InferredStatementContext(final StatementContextBase<?, ?, ?> parent, final StatementContextBase<A, D, E> prototype,
            final CopyType myCopyType, final CopyType childCopyType, final QNameModule targetModule) {
        super(prototype.definition(), CopyHistory.of(myCopyType, prototype.getCopyHistory()));
        this.parent = requireNonNull(parent);
        this.prototype = requireNonNull(prototype);
        this.argument = targetModule == null ? prototype.getStatementArgument()
                : prototype.definition().adaptArgumentValue(prototype, targetModule);
        this.childCopyType = requireNonNull(childCopyType);
        this.targetModule = targetModule;
        this.originalCtx = prototype.getOriginalCtx().orElse(prototype);

        this.substatements = prototype instanceof InferredStatementContext
            ? new Promised(((InferredStatementContext) prototype).substatements()) : null;
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return ImmutableList.of();
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        return mutableEffectiveSubstatements(promiseSubstatements().materialize());
    }

    @Override
    public Iterable<? extends StmtContext<?, ?, ?>> allSubstatements() {
        // No need to concat with declared
        return effectiveSubstatements();
    }

    @Override
    public Stream<? extends StmtContext<?, ?, ?>> allSubstatementsStream() {
        // No need to concat with declared
        return effectiveSubstatements().stream();
    }

    @Override
    public StatementSourceReference getStatementSourceReference() {
        return originalCtx.getStatementSourceReference();
    }

    @Override
    public String rawStatementArgument() {
        return originalCtx.rawStatementArgument();
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
    public D buildDeclared() {
        /*
         * Share original instance of declared statement between all effective statements which have been copied or
         * derived from this original declared statement.
         */
        return originalCtx.buildDeclared();
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef) {
        final Promised promised = promiseSubstatements();
        promised.update(removeStatementFromEffectiveSubstatements(promised.materialize(), statementDef));
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef,
            final String statementArg) {
        final Promised promised = promiseSubstatements();
        promised.update(removeStatementFromEffectiveSubstatements(promised.materialize(), statementDef, statementArg));
    }

    @Override
    public void addEffectiveSubstatement(final Mutable<?, ?, ?> substatement) {
        final Promised promised = promiseSubstatements();
        promised.update(addEffectiveSubstatement(promised.materialize(), substatement));
    }

    @Override
    void addEffectiveSubstatementsImpl(final Collection<? extends Mutable<?, ?, ?>> statements) {
        final Promised promised = promiseSubstatements();
        promised.update(addEffectiveSubstatementsImpl(promised.materialize(), statements));
    }

    @Override
    InferredStatementContext<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        return new InferredStatementContext<>(this, newParent);
    }

    @Override
    boolean hasEmptySubstatements() {
        if (substatements == null) {
            return prototype.hasEmptySubstatements();
        }
        return promiseSubstatements().materialized().isEmpty();
    }

    @Override
    public <X, Z extends EffectiveStatement<X, ?>> @NonNull Optional<X> findSubstatementArgument(
            final @NonNull Class<Z> type) {
        if (substatements instanceof List) {
            return StmtContextDefaults.findSubstatementArgument(this, type);
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
    public boolean hasSubstatement(final @NonNull Class<? extends EffectiveStatement<?, ?>> type) {
        if (substatements instanceof InferredStatementContext.Promised
                && ((InferredStatementContext<?, ?, ?>.Promised) substatements).fullyMaterialized()) {
            return StmtContextDefaults.hasSubstatement(prototype, type);
        }

        // We do not allow deletion of partially-materialized statements, hence this is accurate
        return prototype.hasSubstatement(type);
    }

    @Override
    public <Y extends DeclaredStatement<QName>, Z extends EffectiveStatement<QName, Y>>
            StmtContext<QName, Y, Z> requestSchemaTreeChild(final QName qname) {
        final Promised promised = promiseSubstatements();
        if (promised.fullyMaterialized()) {
            // We have performed materialization, hence we have triggered creation of all our schema tree child
            // statements.
            return null;
        }

        final QName templateQName = qname.bindTo(StmtContextUtils.getRootModuleQName(prototype));
        LOG.debug("Materializing child {} from {}", qname, templateQName);

        final StmtContext<?, ?, ?> template;
        if (prototype instanceof InferredStatementContext) {
            // Note: we need to access namespace here, as the target statement may have already been populated, in which
            //       case we want to obtain the statement in local namespace storage.
            template = (StmtContext) ((InferredStatementContext<?, ?, ?>) prototype).getFromNamespace(
                SchemaTreeNamespace.class, templateQName);
        } else {
            template = prototype.allSubstatementsStream()
                .filter(stmt -> stmt.producesEffective(SchemaTreeEffectiveStatement.class)
                    && templateQName.equals(stmt.getStatementArgument()))
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
        final Mutable<QName, Y, Z> ret = (Mutable<QName, Y, Z>) copySubstatement((Mutable<?, ?, ?>) template)
            .orElseThrow(() -> new InferenceException(getStatementSourceReference(),
                "Failed to materialize child %s template %s", qname, template));
        ensureCompletedPhase(ret);
        promised.addMaterialized(template, ret);

        LOG.debug("Child {} materialized", qname);
        return ret;
    }

    private Substatements substatements() {
        if (substatements == null) {
            substatements = new Promised();
        }
        return substatements;
    }

    private Promised promiseSubstatements() {
        if (substatements instanceof InferredStatementContext.Promised) {
            return (InferredStatementContext<A, D, E>.Promised) substatements;
        } else if (substatements == null) {
            final Promised promised;
            substatements = promised = new Promised();
            return promised;
        } else {
            throw new InferenceException(getStatementSourceReference(), "Unhandled substatements %s", substatements);
        }
    }

    @Override
    Iterable<StatementContextBase<?, ?, ?>> effectiveChildrenToComplete() {
        // When we have not initialized, there are no statements to catch up: we will catch up when we are copying
        // from prototype (which is already at ModelProcessingPhase.EFFECTIVE_MODEL).
        if (substatements == null) {
            return ImmutableList.of();
        } else if (substatements instanceof InferredStatementContext.Promised) {
            return ((InferredStatementContext<?, ?, ?>.Promised) substatements).materialized();
        } else {
            throw new VerifyException("Unhandled substatements " + substatements);
        }
    }

    @Override
    Stream<? extends StmtContext<?, ?, ?>> streamDeclared() {
        return Stream.empty();
    }

    @Override
    Stream<? extends StmtContext<?, ?, ?>> streamEffective() {
        if (substatements instanceof Consumed) {
            return StreamSupport.stream(DeadSpliterator.INSTANCE, false);
        }

        final Promised promised = promiseSubstatements();
        final List<StatementContextBase<?, ?, ?>> materialized = promised.materialize();
        substatements = new Consumed(materialized);
        return materialized.stream();
    }

    private @NonNull List<StatementContextBase<?, ?, ?>> createSubstatements(final @Nullable Substatements upstream,
            final @Nullable Map<StmtContext<?, ?, ?>, StatementContextBase<?, ?, ?>> materializedSchemaTree) {
        final Collection<? extends StatementContextBase<?, ?, ?>> declared = prototype.mutableDeclaredSubstatements();

        // Now talk to prototype based on the promise, otherwise acquire effective substatements
        final Collection<? extends Mutable<?, ?, ?>> effective = upstream != null ? upstream.materialize()
            : prototype.mutableEffectiveSubstatements();

        final List<Mutable<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());

        for (final Mutable<?, ?, ?> stmtContext : declared) {
            if (stmtContext.isSupportedByFeatures()) {
                copySubstatement(stmtContext, buffer, materializedSchemaTree);
            }
        }
        for (final Mutable<?, ?, ?> stmtContext : effective) {
            copySubstatement(stmtContext, buffer, materializedSchemaTree);
        }

        final List<StatementContextBase<?, ?, ?>> ret = beforeAddEffectiveStatementUnsafe(ImmutableList.of(),
            buffer.size());
        ret.addAll((Collection) buffer);
        return ret;
    }

    // Statement copy mess starts here
    //
    // FIXME: This is messy and is probably wrong in some corner case. Even if it is correct, the way how it is correct
    //        relies on hard-coded maps. At the end of the day, the logic needs to be controlled by statement's
    //        StatementSupport.
    // FIXME: YANGTOOLS-652: this map looks very much like UsesStatementSupport.TOP_REUSED_DEF_SET
    private static final ImmutableSet<YangStmtMapping> REUSED_DEF_SET = ImmutableSet.of(
        YangStmtMapping.TYPE,
        YangStmtMapping.TYPEDEF,
        YangStmtMapping.USES);

    private void copySubstatement(final Mutable<?, ?, ?> substatement, final Collection<Mutable<?, ?, ?>> buffer,
            final Map<StmtContext<?, ?, ?>, StatementContextBase<?, ?, ?>> materializedSchemaTree) {
        final StatementDefinition def = substatement.getPublicDefinition();

        // FIXME: YANGTOOLS-652: formerly known as "isReusedByUses"
        if (REUSED_DEF_SET.contains(def)) {
            LOG.debug("Reusing substatement {} for {}", substatement, this);
            buffer.add(substatement);
            return;
        }

        // Consult materialized substatements. We are in a copy operation and will end up throwing materialized
        // statements away -- hence we do not perform Map.remove() to save ourselves a mutation operation.
        //
        // We could also perform a Map.containsKey() and perform a bulk add, but that would mean the statement order
        // against parent would change -- and we certainly do not want that to happen.
        final StatementContextBase<?, ?, ?> materialized = findMaterialized(materializedSchemaTree, substatement);
        if (materialized == null) {
            copySubstatement(substatement).ifPresent(copy -> {
                ensureCompletedPhase(copy);
                buffer.add(copy);
            });
        } else {
            buffer.add(materialized);
        }
    }

    private Optional<? extends Mutable<?, ?, ?>> copySubstatement(final Mutable<?, ?, ?> substatement) {
        return substatement.copyAsChildOf(this, childCopyType, targetModule);
    }

    private static @Nullable StatementContextBase<?, ?, ?> findMaterialized(
            final Map<StmtContext<?, ?, ?>, StatementContextBase<?, ?, ?>> materializedSchemaTree,
            final StmtContext<?, ?, ?> template) {
        return materializedSchemaTree == null ? null : materializedSchemaTree.get(template);
    }

    @SuppressWarnings("unchecked")
    private static List<StatementContextBase<?, ?, ?>> castEffective(final Object substatements) {
        return (List<StatementContextBase<?, ?, ?>>) substatements;
    }

    @SuppressWarnings("unchecked")
    private static HashMap<StmtContext<?, ?, ?>, StatementContextBase<?, ?, ?>> castMaterialized(
            final Object substatements) {
        return (HashMap<StmtContext<?, ?, ?>, StatementContextBase<?, ?, ?>>) substatements;
    }

    // Statement copy mess ends here

    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in SubstatementContext. If any adjustment is made here, make sure it is
     * properly updated there.
     */
    @Override
    @Deprecated
    public Optional<SchemaPath> getSchemaPath() {
        return substatementGetSchemaPath();
    }

    @Override
    public A getStatementArgument() {
        return argument;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return parent;
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.STATEMENT_LOCAL;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentNamespaceStorage() {
        return parent;
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

    @Override
    public boolean isConfiguration() {
        return isConfiguration(parent);
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
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }
}
