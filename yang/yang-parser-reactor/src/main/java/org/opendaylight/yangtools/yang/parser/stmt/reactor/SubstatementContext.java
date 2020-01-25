/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Verify;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

final class SubstatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends
        AbstractResumedStatement<A, D, E> {
    private final StatementContextBase<?, ?, ?> parent;
    private final A argument;

    private volatile SchemaPath schemaPath;

    SubstatementContext(final StatementContextBase<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument) {
        super(def, ref, rawArgument);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = def.parseArgumentValue(this, rawStatementArgument());
    }

    SubstatementContext(final StatementContextBase<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
        final StatementSourceReference ref, final String rawArgument, final A argument, final CopyType copyType) {
        super(def, ref, rawArgument, copyType);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = argument;
    }

    SubstatementContext(final StatementContextBase<A, D, E> original, final StatementContextBase<?, ?, ?> parent,
            final CopyType copyType, final QNameModule targetModule) {
        super(original, copyType);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = targetModule == null ? original.getStatementArgument()
                : original.definition().adaptArgumentValue(original, targetModule);
    }

    private SubstatementContext(final SubstatementContext<A, D, E> original,
            final StatementContextBase<?, ?, ?> parent) {
        super(original);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = original.argument;
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
    public A getStatementArgument() {
        return argument;
    }

    private SchemaPath createSchemaPath() {
        final Optional<SchemaPath> maybeParentPath = parent.getSchemaPath();
        Verify.verify(maybeParentPath.isPresent(), "Parent %s does not have a SchemaPath", parent);
        final SchemaPath parentPath = maybeParentPath.get();

        if (StmtContextUtils.isUnknownStatement(this)) {
            return parentPath.createChild(getPublicDefinition().getStatementName());
        }
        if (argument instanceof QName) {
            final QName qname = (QName) argument;
            if (StmtContextUtils.producesDeclared(this, UsesStatement.class)) {
                return maybeParentPath.orElse(null);
            }

            return parentPath.createChild(qname);
        }
        if (argument instanceof String) {
            // FIXME: This may yield illegal argument exceptions
            final Optional<StmtContext<?, ?, ?>> originalCtx = getOriginalCtx();
            final QName qname = StmtContextUtils.qnameFromArgument(originalCtx.orElse(this), (String) argument);
            return parentPath.createChild(qname);
        }
        if (argument instanceof SchemaNodeIdentifier
                && (StmtContextUtils.producesDeclared(this, AugmentStatement.class)
                        || StmtContextUtils.producesDeclared(this, RefineStatement.class)
                        || StmtContextUtils.producesDeclared(this, DeviationStatement.class))) {

            return parentPath.createChild(((SchemaNodeIdentifier) argument).getPathFromRoot());
        }

        // FIXME: this does not look right
        return maybeParentPath.orElse(null);
    }

    @Override
    public Optional<SchemaPath> getSchemaPath() {
        SchemaPath local = schemaPath;
        if (local == null) {
            synchronized (this) {
                local = schemaPath;
                if (local == null) {
                    local = createSchemaPath();
                    schemaPath = local;
                }
            }
        }

        return Optional.ofNullable(local);
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
    public YangVersion getRootVersion() {
        return getRoot().getRootVersion();
    }

    @Override
    public void setRootVersion(final YangVersion version) {
        getRoot().setRootVersion(version);
    }

    @Override
    public void addMutableStmtToSeal(final MutableStatement mutableStatement) {
        getRoot().addMutableStmtToSeal(mutableStatement);
    }

    @Override
    public void addRequiredSource(final SourceIdentifier dependency) {
        getRoot().addRequiredSource(dependency);
    }

    @Override
    public void setRootIdentifier(final SourceIdentifier identifier) {
        getRoot().setRootIdentifier(identifier);
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

    @Override
    StatementContextBase<?, ?, ?> reparent(final StatementContextBase<?, ?, ?> result) {
        return new SubstatementContext<>(this, result);
    }
}
