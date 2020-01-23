/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
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
    private static final VarHandle SCHEMA_PATH;
    private static final VarHandle CONFIGURATION;
    private static final VarHandle IGNORE_CONFIG;
    private static final VarHandle IGNORE_IF_FEATURE;

    static {
        final Lookup lookup = MethodHandles.lookup();

        try {
            SCHEMA_PATH = lookup.findVarHandle(SubstatementContext.class, "schemaPath", Object.class);
            CONFIGURATION = lookup.findVarHandle(SubstatementContext.class, "configuration", byte.class);
            IGNORE_CONFIG = lookup.findVarHandle(SubstatementContext.class, "ignoreConfig", byte.class);
            IGNORE_IF_FEATURE = lookup.findVarHandle(SubstatementContext.class, "ignoreIfFeature", byte.class);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final AbstractStmtContext<?, ?, ?> parent;
    private final A argument;

    // Used through CONFIGURATION
    @SuppressWarnings("unused")
    private byte configuration;

    /**
     * This field maintains a resolution cache for ignore config, so once we have returned a result, we will
     * keep on returning the same result without performing any lookups.
     */
    // Used through IGNORE_CONFIG
    @SuppressWarnings("unused")
    private byte ignoreConfig;

    /**
     * This field maintains a resolution cache for ignore if-feature, so once we have returned a result, we will
     * keep on returning the same result without performing any lookups.
     */
    // Used through IGNORE_IF_FEATURE
    @SuppressWarnings("unused")
    private byte ignoreIfFeature;

    // Accessed through SCHEMA_PATH
    @SuppressWarnings("unused")
    private volatile Object schemaPath;

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
        return substatementSchemaPath(this, SCHEMA_PATH);
    }

    @Override
    public boolean isConfiguration() {
        return substatementIsConfiguration(this, CONFIGURATION);
    }

    @Override
    public boolean isEnabledSemanticVersioning() {
        return parent.isEnabledSemanticVersioning();
    }

    @Override
    protected boolean isIgnoringIfFeatures() {
        return substatementIsIgnoringIfFeatures(this, IGNORE_IF_FEATURE);
    }

    @Override
    protected boolean isIgnoringConfig() {
        return substatementIsIgnoringConfig(this, IGNORE_CONFIG);
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
