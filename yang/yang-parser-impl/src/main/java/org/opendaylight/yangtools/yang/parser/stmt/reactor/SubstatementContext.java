/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.util.OptionalBoolean;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.AugmentToChoiceNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;

final class SubstatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends
        StatementContextBase<A, D, E> {
    private final StatementContextBase<?, ?, ?> parent;
    private final A argument;

    /**
     * config statements are not all that common which means we are performing a recursive search towards the root
     * every time {@link #isConfiguration()} is invoked. This is quite expensive because it causes a linear search
     * for the (usually non-existent) config statement.
     *
     * This field maintains a resolution cache, so once we have returned a result, we will keep on returning the same
     * result without performing any lookups.
     */
    // BooleanField value
    private byte configuration;

    /**
     * This field maintains a resolution cache for ignore config, so once we have returned a result, we will
     * keep on returning the same result without performing any lookups.
     */
    // BooleanField value
    private byte ignoreConfig;

    /**
     * This field maintains a resolution cache for ignore if-feature, so once we have returned a result, we will
     * keep on returning the same result without performing any lookups.
     */
    // BooleanField value
    private byte ignoreIfFeature;

    private volatile SchemaPath schemaPath;

    SubstatementContext(final StatementContextBase<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument) {
        super(def, ref, rawArgument);
        this.parent = Preconditions.checkNotNull(parent, "Parent must not be null");
        this.argument = def.parseArgumentValue(this, rawStatementArgument());
    }

    SubstatementContext(final StatementContextBase<A, D, E> original, final StatementContextBase<?, ?, ?> parent,
            final CopyType copyType, final QNameModule targetModule) {
        super(original, copyType);
        this.parent = Preconditions.checkNotNull(parent);
        this.argument = targetModule == null ? original.getStatementArgument()
                : original.definition().adaptArgumentValue(original, targetModule);
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

    @Nonnull
    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

    @Override
    public A getStatementArgument() {
        return argument;
    }

    private boolean isSupportedAsShorthandCase() {
        final Collection<?> supportedCaseShorthands = getFromNamespace(ValidationBundlesNamespace.class,
                ValidationBundleType.SUPPORTED_CASE_SHORTHANDS);
        return supportedCaseShorthands == null || supportedCaseShorthands.contains(getPublicDefinition());
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

            final SchemaPath path;
            if ((StmtContextUtils.producesDeclared(getParentContext(), ChoiceStatement.class)
                    || Boolean.TRUE.equals(parent.getFromNamespace(AugmentToChoiceNamespace.class, parent)))
                    && isSupportedAsShorthandCase()) {
                path = parentPath.createChild(qname);
            } else {
                path = parentPath;
            }
            return path.createChild(qname);
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

    @Nonnull
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
        if (isIgnoringConfig()) {
            return true;
        }

        if (OptionalBoolean.isPresent(configuration)) {
            return OptionalBoolean.get(configuration);
        }

        final StmtContext<Boolean, ?, ?> configStatement = StmtContextUtils.findFirstSubstatement(this,
            ConfigStatement.class);
        final boolean parentIsConfig = parent.isConfiguration();

        final boolean isConfig;
        if (configStatement != null) {
            isConfig = configStatement.getStatementArgument();

            // Validity check: if parent is config=false this cannot be a config=true
            InferenceException.throwIf(isConfig && !parentIsConfig, getStatementSourceReference(),
                    "Parent node has config=false, this node must not be specifed as config=true");
        } else {
            // If "config" statement is not specified, the default is the same as the parent's "config" value.
            isConfig = parentIsConfig;
        }

        // Resolved, make sure we cache this return
        configuration = OptionalBoolean.of(isConfig);
        return isConfig;
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
    public void addRequiredModule(final ModuleIdentifier dependency) {
        getRoot().addRequiredModule(dependency);
    }

    @Override
    public void setRootIdentifier(final ModuleIdentifier identifier) {
        getRoot().setRootIdentifier(identifier);
    }

    @Override
    protected boolean isIgnoringIfFeatures() {
        if (OptionalBoolean.isPresent(ignoreIfFeature)) {
            return OptionalBoolean.get(ignoreIfFeature);
        }

        final boolean ret = definition().isIgnoringIfFeatures() || parent.isIgnoringIfFeatures();
        ignoreIfFeature = OptionalBoolean.of(ret);

        return ret;
    }

    @Override
    protected boolean isIgnoringConfig() {
        if (OptionalBoolean.isPresent(ignoreConfig)) {
            return OptionalBoolean.get(ignoreConfig);
        }

        final boolean ret = definition().isIgnoringConfig() || parent.isIgnoringConfig();
        ignoreConfig = OptionalBoolean.of(ret);

        return ret;
    }

    @Override
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }
}
