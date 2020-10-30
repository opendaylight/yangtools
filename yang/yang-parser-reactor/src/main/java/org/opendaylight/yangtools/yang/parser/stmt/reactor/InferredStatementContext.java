/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

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
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.OnDemandSchemaTreeStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
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

    private Map<StmtContext<?, ?, ?>, Mutable<QName, ?, ?>> materializedSchemaTree = null;

    private InferredStatementContext(final InferredStatementContext<A, D, E> original,
            final StatementContextBase<?, ?, ?> parent) {
        super(original);
        this.parent = requireNonNull(parent);
        this.childCopyType = original.childCopyType;
        this.targetModule = original.targetModule;
        this.prototype = original.prototype;
        this.originalCtx = original.originalCtx;
        this.argument = original.argument;
        setSubstatementsInitialized();
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

        // Note: substatements from prototype are initialized lazily through ensureSubstatements()
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return ImmutableList.of();
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
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        ensureSubstatements();
        return super.mutableEffectiveSubstatements();
    }

    @Override
    public void addEffectiveSubstatement(final Mutable<?, ?, ?> substatement) {
        ensureSubstatements();
        super.addEffectiveSubstatement(substatement);
    }

    @Override
    public void addEffectiveSubstatements(final Collection<? extends Mutable<?, ?, ?>> statements) {
        ensureSubstatements();
        super.addEffectiveSubstatements(statements);
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef,
            final String statementArg) {
        ensureSubstatements();
        super.removeStatementFromEffectiveSubstatements(statementDef, statementArg);
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef) {
        ensureSubstatements();
        super.removeStatementFromEffectiveSubstatements(statementDef);
    }

    @Override
    InferredStatementContext<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        return new InferredStatementContext<>(this, newParent);
    }

    @Override
    boolean hasEmptySubstatements() {
        ensureSubstatements();
        return hasEmptyEffectiveSubstatements();
    }

    @Override
    public <D extends DeclaredStatement<QName>, E extends EffectiveStatement<QName, D>>
            StmtContext<QName, D, E> requestSchemaTreeChild(final QName qname) {
        if (substatementsInitialized()) {
            // We have performed materialization, hence we have nothing more to contribute to the namespace.
            return null;
        }

        final QName templateQName = qname.bindTo(StmtContextUtils.getRootModuleQName(prototype));
        LOG.debug("Materializing child {} from {}", qname, templateQName);

        final StmtContext<?, ?, ?> template = prototype.allSubstatementsStream()
            .filter(stmt -> stmt.producesEffective(SchemaTreeEffectiveStatement.class))
            .filter(stmt -> templateQName.equals(stmt.getStatementArgument()))
            .findFirst()
            .orElse(null);
        if (template == null) {
            // We do not have a template, this child does not exist. It may be added later, but that is someone else's
            // responsibility.
            LOG.debug("Child {} does not have a template", qname);
            return null;
        }

        if (!template.isSupportedByFeatures()) {
            // FIXME: YANGTOOLS-859: we should be signalling this condition separately
            LOG.debug("Child {} is not supported by features, not materializing it", qname);
            return null;
        }

        @SuppressWarnings("unchecked")
        final Mutable<QName, D, E> ret = (Mutable<QName, D, E>) copySubstatement(prototype).orElseThrow(
            () -> new InferenceException(getStatementSourceReference(),
                "Failed to materialize child %s template %s", qname, template));
        ensureCompletedPhase(ret);
        addMaterialized(template, ret);

        LOG.debug("Child {} materialized", qname);
        return ret;
    }

    // Instantiate this statement's effective substatements. Note this method has side-effects in namespaces and overall
    // BuildGlobalContext, hence it must be called at most once.
    private void ensureSubstatements() {
        if (!substatementsInitialized()) {
            initializeSubstatements();
        }
    }

    private void initializeSubstatements() {
        final Collection<? extends StatementContextBase<?, ?, ?>> declared = prototype.mutableDeclaredSubstatements();
        final Collection<? extends Mutable<?, ?, ?>> effective = prototype.mutableEffectiveSubstatements();
        final List<Mutable<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());

        for (final Mutable<?, ?, ?> stmtContext : declared) {
            if (stmtContext.isSupportedByFeatures()) {
                copySubstatement(stmtContext, buffer);
            }
        }
        for (final Mutable<?, ?, ?> stmtContext : effective) {
            copySubstatement(stmtContext, buffer);
        }

        // We are bypassing usual safeties here, as this is not introducing new statements but rather just materializing
        // them when the need has arised.
        addInitialEffectiveSubstatements(buffer);

        // Clean up after requestSchemaTreeChild(), at this point that method will do nothing anyway.
        materializedSchemaTree = null;
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

    private void copySubstatement(final Mutable<?, ?, ?> substatement, final Collection<Mutable<?, ?, ?>> buffer) {
        final StatementDefinition def = substatement.getPublicDefinition();

        // FIXME: YANGTOOLS-652: formerly known as "isReusedByUses"
        if (REUSED_DEF_SET.contains(def)) {
            LOG.debug("Reusing substatement {} for {}", substatement, this);
            buffer.add(substatement);
            return;
        }

        final Mutable<?, ?, ?> materialized = findMaterialized(substatement);
        if (materialized != null) {
            buffer.add(materialized);
        } else {
            copySubstatement(substatement).ifPresent(copy -> {
                ensureCompletedPhase(copy);
                buffer.add(copy);
            });
        }
    }

    private void addMaterialized(final StmtContext<?, ?, ?> template, final Mutable<QName, ?, ?> copy) {
        if (materializedSchemaTree == null) {
            materializedSchemaTree = new HashMap<>();
        }

        final StmtContext<QName, ?, ?> existing = materializedSchemaTree.put(template, copy);
        if (existing != null) {
            throw new VerifyException(
                "Unexpected duplicate request for " + copy.getStatementArgument() + " previous result was " + existing);
        }
    }

    private @Nullable Mutable<QName, ?, ?> findMaterialized(final StmtContext<?, ?, ?> template) {
        return materializedSchemaTree == null ? null : materializedSchemaTree.get(template);
    }

    private Optional<? extends Mutable<?, ?, ?>> copySubstatement(final Mutable<?, ?, ?> substatement) {
        return substatement.copyAsChildOf(this, childCopyType, targetModule);
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
    public NamespaceStorageNode getParentNamespaceStorage() {
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
