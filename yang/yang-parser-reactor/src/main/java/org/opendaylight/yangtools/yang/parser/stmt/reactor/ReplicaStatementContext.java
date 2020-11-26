/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A replica of a different statement. It does not allow modification, but produces an effective statement from a
 * designated source.
 */
final class ReplicaStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> {
    private static final Logger LOG = LoggerFactory.getLogger(ReplicaStatementContext.class);

    private final StatementContextBase<?, ?, ?> parent;
    private final StatementContextBase<A, D, E> source;

    private final boolean haveRef;

    ReplicaStatementContext(final StatementContextBase<?, ?, ?> parent, final StatementContextBase<A, D, E> source) {
        super(source);
        this.parent = requireNonNull(parent);
        this.source = requireNonNull(source);
        if (source.isSupportedToBuildEffective()) {
            source.incRef();
            haveRef = true;
        } else {
            setIsSupportedToBuildEffective(false);
            haveRef = false;
        }
    }

    @Override
    E createEffective() {
        return source.buildEffective();
    }

    @Override
    public boolean isConfiguration() {
        return source.isConfiguration();
    }

    @Override
    public D buildDeclared() {
        return source.buildDeclared();
    }

    @Override
    public A argument() {
        return source.argument();
    }

    @Override
    public StatementSourceReference sourceReference() {
        return source.sourceReference();
    }

    @Override
    public String rawArgument() {
        return source.rawArgument();
    }

    @Override
    public Optional<StmtContext<A, D, E>> getOriginalCtx() {
        return source.getOriginalCtx();
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return source.mutableDeclaredSubstatements();
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        return source.mutableEffectiveSubstatements();
    }

    @Override
    boolean hasEmptySubstatements() {
        return source.hasEmptySubstatements();
    }

    @Override
    Iterable<StatementContextBase<?, ?, ?>> effectiveChildrenToComplete() {
        return ImmutableList.of();
    }

    @Override
    int sweepSubstatements() {
        if (haveRef) {
            source.decRef();
        }
        return 0;
    }

    @Override
    public Optional<StmtContext<A, D, E>> getPreviousCopyCtx() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef,
            final String statementArg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEffectiveSubstatement(final Mutable<?, ?, ?> substatement) {
        throw new UnsupportedOperationException();
    }

    @Override
    void addEffectiveSubstatementsImpl(final Collection<? extends Mutable<?, ?, ?>> statements) {
        throw new UnsupportedOperationException();
    }

    @Override
    Stream<? extends StmtContext<?, ?, ?>> streamDeclared() {
        throw new UnsupportedOperationException();
    }

    @Override
    Stream<? extends StmtContext<?, ?, ?>> streamEffective() {
        throw new UnsupportedOperationException();
    }

    @Override
    StatementContextBase<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        throw new UnsupportedOperationException();
    }

    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in InferredStatementContext/SubstatementContext. If any adjustment is made
     * here, make sure it is properly updated there.
     */
    @Override
    @Deprecated
    Optional<SchemaPath> schemaPath() {
        return substatementGetSchemaPath();
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
