/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

final class SubstatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends
        StatementContextBase<A, D, E> implements ChildStmtContext<A, D, E> {
    private final AbstractStmtContext<?, ?, ?> parent;
    private final A argument;

    SubstatementContext(final AbstractStmtContext<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument) {
        super(def, ref, rawArgument);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = def.parseArgumentValue(this, rawStatementArgument());
    }

    SubstatementContext(final AbstractStmtContext<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
        final StatementSourceReference ref, final String rawArgument, final A argument, final CopyType copyType) {
        super(def, ref, rawArgument, copyType);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = argument;
    }

    private SubstatementContext(final StatementContextBase<A, D, E> original,
            final AbstractStmtContext<?, ?, ?> parent) {
        super(original);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = original.getStatementArgument();
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
    public A getStatementArgument() {
        return argument;
    }

    @Override
    public Optional<SchemaPath> getSchemaPath() {
        return substatementSchemaPath();
    }

    @Override
    public boolean isConfiguration() {
        return substatementIsConfiguration(parent);
    }

    @Override
    public boolean isEnabledSemanticVersioning() {
        return parent.isEnabledSemanticVersioning();
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
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }

    @Override
    SubstatementContext<A, D, E> reparent(final AbstractStmtContext<?, ?, ?> newParent) {
        return new SubstatementContext<>(this, parent);
    }
}
