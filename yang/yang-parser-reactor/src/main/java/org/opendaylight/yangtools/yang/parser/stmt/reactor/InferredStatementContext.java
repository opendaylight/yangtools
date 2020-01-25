/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A statement which has been inferred to exist. Functionally it is equivalent to a SubstatementContext, but it is not
 * backed by a declaration (and declared statements). It is backed by a prototype StatementContextBase and has only
 * effective substatements, which are either transformed from that prototype or added by inference.
 */
final class InferredStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> {
    private static final Logger LOG = LoggerFactory.getLogger(InferredStatementContext.class);

    private final @NonNull StatementContextBase<A, D, E> prototype;
    private final @NonNull StatementContextBase<?, ?, ?> parent;
    private final @NonNull CopyType childCopyType;
    private final QNameModule targetModule;
    private final A argument;

    private InferredStatementContext(final InferredStatementContext<A, D, E> original,
            final StatementContextBase<?, ?, ?> parent) {
        super(original);
        this.parent = requireNonNull(parent);
        this.childCopyType = original.childCopyType;
        this.targetModule = original.targetModule;
        this.prototype = original.prototype;
        this.argument = original.argument;
    }

    InferredStatementContext(final StatementContextBase<?, ?, ?> parent, final StatementContextBase<A, D, E> prototype,
            final CopyType myCopyType, final CopyType childCopyType, final QNameModule targetModule) {
        super(CopyHistory.of(myCopyType, prototype.getCopyHistory()));
        this.parent = requireNonNull(parent);
        this.prototype = requireNonNull(prototype);
        this.argument = targetModule == null ? prototype.getStatementArgument()
                : prototype.definition().adaptArgumentValue(prototype, targetModule);
        this.childCopyType = requireNonNull(childCopyType);
        this.targetModule = targetModule;

        // FIXME: YANGTOOLS-784: instantiate these lazily
        addEffectiveSubstatements(createEffective());
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements() {
        return List.of();
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return List.of();
    }

    @Override
    public StatementSourceReference getStatementSourceReference() {
        return prototype.getStatementSourceReference();
    }

    @Override
    public String rawStatementArgument() {
        return prototype.rawStatementArgument();
    }

    @Override
    public Optional<StmtContext<?, ?, ?>> getOriginalCtx() {
        final Optional<StmtContext<?, ?, ?>> orig = prototype.getOriginalCtx();
        return orig.isPresent() ? orig : Optional.of(prototype);
    }

    @Override
    public Optional<? extends StmtContext<?, ?, ?>> getPreviousCopyCtx() {
        return Optional.of(prototype);
    }

    @Override
    protected StatementDefinitionContext<A, D, E> definition() {
        return prototype.definition();
    }

    @Override
    InferredStatementContext<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        return new InferredStatementContext<>(this, newParent);
    }

    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in InferredStatementContext. If any adjustment is made here, make sure it
     * is properly updated there.
     */
    @Override
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
    public Registry getBehaviourRegistry() {
        return parent.getBehaviourRegistry();
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
    public boolean isEnabledSemanticVersioning() {
        return parent.isEnabledSemanticVersioning();
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

    private List<Mutable<?, ?, ?>> createEffective() {
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

        return buffer;
    }

    private void copySubstatement(final Mutable<?, ?, ?> stmtContext, final Collection<Mutable<?, ?, ?>> buffer) {
        if (needToCopyByUses(stmtContext)) {
            final Mutable<?, ?, ?> copy = childCopyOf(stmtContext, childCopyType, targetModule);
            LOG.debug("Copying substatement {} for {} as {}", stmtContext, this, copy);
            buffer.add(copy);
        } else if (isReusedByUses(stmtContext)) {
            LOG.debug("Reusing substatement {} for {}", stmtContext, this);
            buffer.add(stmtContext);
        } else {
            LOG.debug("Skipping statement {}", stmtContext);
        }
    }

    // FIXME: revise this, as it seems to be wrong
    private static final ImmutableSet<YangStmtMapping> NOCOPY_FROM_GROUPING_SET = ImmutableSet.of(
        YangStmtMapping.DESCRIPTION,
        YangStmtMapping.REFERENCE,
        YangStmtMapping.STATUS);
    private static final ImmutableSet<YangStmtMapping> REUSED_DEF_SET = ImmutableSet.of(
        YangStmtMapping.TYPE,
        YangStmtMapping.TYPEDEF,
        YangStmtMapping.USES);

    private static boolean needToCopyByUses(final StmtContext<?, ?, ?> stmtContext) {
        final StatementDefinition def = stmtContext.getPublicDefinition();
        if (REUSED_DEF_SET.contains(def)) {
            LOG.debug("Will reuse {} statement {}", def, stmtContext);
            return false;
        }
        if (NOCOPY_FROM_GROUPING_SET.contains(def)) {
            return !YangStmtMapping.GROUPING.equals(stmtContext.coerceParentContext().getPublicDefinition());
        }

        LOG.debug("Will copy {} statement {}", def, stmtContext);
        return true;
    }

    private static boolean isReusedByUses(final StmtContext<?, ?, ?> stmtContext) {
        return REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
    }
}
