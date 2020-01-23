/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A child-proxying {@link Mutable} context, which can be explicitly queried for specific schema tree child.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public final class CopiedStmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends AbstractStmtContext<A, D, E> implements ChildStmtContext<A, D, E> {
    private static final Logger LOG = LoggerFactory.getLogger(CopiedStmtContext.class);

    private final @NonNull AbstractStmtContext<A, D, E> original;
    private final @NonNull AbstractStmtContext<?, ?, ?> parent;
    private final @NonNull CopyType childCopyType;
    private final QNameModule targetModule;
    private final A argument;

    // FIXME: YANGTOOLS-784: lazy substatements:
    //        - IdentityHashMap<StmtContext<?, ?, ?>, StmtContext<?, ?, ?>> for materialized children,
    //          key being original context, value being materialized context
    //        - we will also need removed/replaced/added statements, somehow
    private final List<Mutable<?, ?, ?>> effective;

    private CopiedStmtContext(final AbstractStmtContext<?, ?, ?> parent, final CopiedStmtContext<A, D, E> old) {
        super(old.getCopyHistory());
        this.parent = requireNonNull(parent);
        this.original = old.original;
        this.argument = old.argument;
        this.childCopyType = old.childCopyType;
        this.targetModule = old.targetModule;
        this.effective = old.effective;
    }

    CopiedStmtContext(final AbstractStmtContext<?, ?, ?> parent, final AbstractStmtContext<A, D, E> original,
            final CopyType myCopyType, final CopyType childCopyType, final QNameModule targetModule) {
        super(CopyHistory.of(myCopyType, original.getCopyHistory()));
        this.parent = requireNonNull(parent);
        this.original = requireNonNull(original);
        this.argument = targetModule == null ? original.getStatementArgument()
                : original.definition().adaptArgumentValue(original, targetModule);
        this.childCopyType = requireNonNull(childCopyType);

        // TODO: when is this null?
        this.targetModule = targetModule;

        // FIXME: YANGTOOLS-784: populate these lazily
        effective = copyFrom();
    }

    /**
     * Find an inferred child node. Caller indicates that its inference logic has determined that specified
     * schema tree child should exist and expects an inference guidance as follows:
     * <ul>
     *   <li>A present value is returned if the node has been found</li>
     *   <li>{@code InferenceException} is thrown if the node has not been found</li>
     *   <li>An absent value is returned if the node has been found, but conformance options (or other logic)
     *       indicates any further inference does not have an effect on result of computation and this node
     *       should be ignored.</li>
     * </ul>
     *
     * @param qname resolved node identifier of child node
     * @return An optional mutable context, empty if the inferred node exists, but has been determined to have
     *         no effect on overall result.
     * @throws NullPointerException if qname is null
     * @throws InferenceException if the specified child does not exist
     */
    public Optional<Mutable<?, ?, ?>> inferSchemaTreeChild(final QName qname) {
        throw new UnsupportedOperationException();
    }

    @Override
    public A getStatementArgument() {
        return argument;
    }

    @Override
    public AbstractStmtContext<?, ?, ?> parent() {
        return parent;
    }

    @Override
    public @NonNull AbstractStmtContext<?, ?, ?> getParentContext() {
        return parent;
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

    @Override
    public StatementSource getStatementSource() {
        return original.getStatementSource();
    }

    @Override
    public StatementSourceReference getStatementSourceReference() {
        return original.getStatementSourceReference();
    }

    @Override
    public String rawStatementArgument() {
        return original.rawStatementArgument();
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
    public Optional<StmtContext<?, ?, ?>> getOriginalCtx() {
        final Optional<StmtContext<?, ?, ?>> orig = original.getOriginalCtx();
        return orig.isPresent() ? orig : Optional.of(original);
    }

    @Override
    public Optional<? extends StmtContext<?, ?, ?>> getPreviousCopyCtx() {
        return Optional.of(original);
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableDeclaredSubstatements() {
        return List.of();
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        return Collections.unmodifiableCollection(effective);
    }

    @Override
    protected StatementDefinitionContext<A, D, E> definition() {
        return original.definition();
    }

    @Override
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }

    @Override
    public Optional<SchemaPath> getSchemaPath() {
        return substatementSchemaPath();
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef) {
        if (!effective.isEmpty()) {
            removeStatement(effective.iterator(), statementDef);
        }
    }

    @Override
    public boolean isConfiguration() {
        return substatementIsConfiguration(parent);
    }

    @Override
    protected boolean isIgnoringIfFeatures() {
        return substatementIsIgnoringIfFeatures(parent);
    }

    @Override
    protected boolean isIgnoringConfig() {
        return substatementIsIgnoringConfig(parent);
    }

    @Override
    protected void appendEffectiveStatement(final Mutable<?, ?, ?> substatement) {
        effective.add(substatement);
    }

    @Override
    protected void appendEffectiveStatements(final Collection<? extends Mutable<?, ?, ?>> statements) {
        effective.addAll(statements);
    }

    @Override
    void doRemoveStatement(final StatementDefinition statementDef, final String statementArg) {
        if (!effective.isEmpty()) {
            removeStatement(effective.iterator(), statementDef, statementArg);
        }
    }

    @Override
    CopiedStmtContext<A, D, E> reparent(final AbstractStmtContext<?, ?, ?> newParent) {
        return new CopiedStmtContext<>(newParent, this);
    }

    @Override
    Collection<? extends AbstractStmtContext<?, ?, ?>> declared() {
        return List.of();
    }

    @Override
    Collection<? extends Mutable<?, ?, ?>> effective() {
        return effective;
    }

    private List<Mutable<?, ?, ?>> copyFrom() {
        final Collection<? extends AbstractStmtContext<?, ?, ?>> origDeclared = original.declared();
        final Collection<? extends Mutable<?, ?, ?>> origEffective = original.effective();
        final List<Mutable<?, ?, ?>> result = new ArrayList<>(origDeclared.size() + origEffective.size());

        for (final Mutable<?, ?, ?> stmtContext : origDeclared) {
            if (stmtContext.isSupportedByFeatures()) {
                copySubstatement(stmtContext, childCopyType, targetModule, result);
            }
        }

        for (final Mutable<?, ?, ?> stmtContext : origEffective) {
            copySubstatement(stmtContext, childCopyType, targetModule, result);
        }

        return result;
    }

    private void copySubstatement(final Mutable<?, ?, ?> stmtContext, final CopyType typeOfCopy,
            final QNameModule newQNameModule, final Collection<Mutable<?, ?, ?>> buffer) {
        if (needToCopyByUses(stmtContext)) {
            final Mutable<?, ?, ?> copy = childCopyOf(stmtContext, typeOfCopy, newQNameModule);
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
