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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.GlobalNamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

final class SubstatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends
        AbstractResumedStatement<A, D, E> {
    private final StatementContextBase<?, ?, ?> parent;
    private final A argument;

    private SubstatementContext(final SubstatementContext<A, D, E> original,
            final StatementContextBase<?, ?, ?> parent) {
        super(original);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = original.argument;
    }

    SubstatementContext(final StatementContextBase<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument) {
        super(def, ref, rawArgument);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = def.parseArgumentValue(this, rawStatementArgument());
    }

    // FIXME: YANGTOOLS-784: this constructor is only called in contexts where a different implementation
    //                       would be more appropriate
    SubstatementContext(final StatementContextBase<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument, final A argument, final CopyType copyType) {
        super(def, ref, rawArgument, copyType);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = argument;
    }

    @Override
    SubstatementContext<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        return new SubstatementContext<>(this, newParent);
    }

    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in InferredStatementContext. If any adjustment is made here, make sure it is
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
    public GlobalNamespaceStorageNode getGlobalNamespaceStorage() {
        return parent.getGlobalNamespaceStorage();
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
