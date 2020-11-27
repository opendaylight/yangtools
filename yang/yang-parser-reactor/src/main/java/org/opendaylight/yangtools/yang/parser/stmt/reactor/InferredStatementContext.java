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
    private static final Logger LOG = LoggerFactory.getLogger(InferredStatementContext.class);

    private final @NonNull StatementContextBase<A, D, E> prototype;
    private final @NonNull StatementContextBase<?, ?, ?> parent;
    private final @NonNull StmtContext<A, D, E> originalCtx;
    private final @NonNull CopyType childCopyType;
    private final QNameModule targetModule;
    private final A argument;

    /**
     * Effective substatements, lazily materialized. This field can have three states:
     * <ul>
     *   <li>it can be {@code null}, in which case no materialization has taken place</li>
     *   <li>it can be a {@link HashMap}, in which case partial materialization has taken place</li>
     *   <li>it can be a {@link List}, in which case full materialization has taken place</li>
     * </ul>
     */
    private Object substatements;

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
        this.substatements = ImmutableList.of();
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
    public D buildDeclared() {
        /*
         * Share original instance of declared statement between all effective statements which have been copied or
         * derived from this original declared statement.
         */
        return originalCtx.buildDeclared();
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
    boolean hasEmptySubstatements() {
        if (substatements == null) {
            return prototype.hasEmptySubstatements();
        }
        return substatements instanceof HashMap ? false : ((List<?>) substatements).isEmpty();
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
        return substatements instanceof List ? StmtContextDefaults.hasSubstatement(prototype, type)
            // We do not allow deletion of partially-materialized statements, hence this is accurate
            : prototype.hasSubstatement(type);
    }

    @Override
    public <Y extends DeclaredStatement<QName>, Z extends EffectiveStatement<QName, Y>>
            StmtContext<QName, Y, Z> requestSchemaTreeChild(final QName qname) {
        if (substatements instanceof List) {
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
        addMaterialized(template, ret);

        LOG.debug("Child {} materialized", qname);
        return ret;
    }

    // Instantiate this statement's effective substatements. Note this method has side-effects in namespaces and overall
    // BuildGlobalContext, hence it must be called at most once.
    private List<StatementContextBase<?, ?, ?>> ensureEffectiveSubstatements() {
        return substatements instanceof List ? castEffective(substatements)
            : initializeSubstatements(castMaterialized(substatements));
    }

    @Override
    Iterable<StatementContextBase<?, ?, ?>> effectiveChildrenToComplete() {
        // When we have not initialized, there are no statements to catch up: we will catch up when we are copying
        // from prototype (which is already at ModelProcessingPhase.EFFECTIVE_MODEL).
        if (substatements == null) {
            return ImmutableList.of();
        } else if (substatements instanceof HashMap) {
            return castMaterialized(substatements).values();
        } else {
            return castEffective(substatements);
        }
    }

    @Override
    Stream<? extends StmtContext<?, ?, ?>> streamDeclared() {
        return Stream.empty();
    }

    @Override
    Stream<? extends StmtContext<?, ?, ?>> streamEffective() {
        // FIXME: YANGTOOLS-1184: do not force initialization
        return ensureEffectiveSubstatements().stream();
    }

    private List<StatementContextBase<?, ?, ?>> initializeSubstatements(
            final Map<StmtContext<?, ?, ?>, StatementContextBase<?, ?, ?>> materializedSchemaTree) {
        final Collection<? extends StatementContextBase<?, ?, ?>> declared = prototype.mutableDeclaredSubstatements();
        final Collection<? extends Mutable<?, ?, ?>> effective = prototype.mutableEffectiveSubstatements();
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
        substatements = ret;
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

    private void addMaterialized(final StmtContext<?, ?, ?> template, final Mutable<?, ?, ?> copy) {
        final HashMap<StmtContext<?, ?, ?>, StatementContextBase<?, ?, ?>> materializedSchemaTree;
        if (substatements == null) {
            // Lazy initialization of backing map. We do not expect this to be used often or multiple times -- each hit
            // here means an inference along schema tree, such as deviate/augment. HashMap requires power-of-two and
            // defaults to 0.75 load factor -- we therefore size it to 4, i.e. next two inserts will not cause a
            // resizing operation.
            materializedSchemaTree = new HashMap<>(4);
            substatements = materializedSchemaTree;
        } else {
            verify(substatements instanceof HashMap, "Unexpected substatements %s", substatements);
            materializedSchemaTree = castMaterialized(substatements);
        }

        final StmtContext<?, ?, ?> existing = materializedSchemaTree.put(template,
            (StatementContextBase<?, ?, ?>) copy);
        if (existing != null) {
            throw new VerifyException(
                "Unexpected duplicate request for " + copy.getStatementArgument() + " previous result was " + existing);
        }
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
    Optional<SchemaPath> schemaPath() {
        return substatementGetSchemaPath();
    }

    @Override
    public A argument() {
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
