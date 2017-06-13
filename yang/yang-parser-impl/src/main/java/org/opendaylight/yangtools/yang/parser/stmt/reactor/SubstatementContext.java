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
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameCacheNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.AugmentToChoiceNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SubstatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends
        StatementContextBase<A, D, E> {
    private static final Logger LOG = LoggerFactory.getLogger(SubstatementContext.class);

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
    private boolean haveConfiguration;
    private boolean configuration;
    private boolean wasCheckedIfInYangDataExtensionBody;
    private boolean isInYangDataExtensionBody;

    private volatile SchemaPath schemaPath;

    SubstatementContext(final StatementContextBase<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument) {
        super(def, ref, rawArgument);
        this.parent = Preconditions.checkNotNull(parent, "Parent must not be null");
        this.argument = def.parseArgumentValue(this, rawStatementArgument());
    }

    @SuppressWarnings("unchecked")
    private SubstatementContext(final SubstatementContext<A, D, E> original, final QNameModule newQNameModule,
            final StatementContextBase<?, ?, ?> newParent, final CopyType copyType) {
        super(original, copyType);
        this.parent = Preconditions.checkNotNull(newParent);

        if (newQNameModule != null) {
            final A originalArg = original.argument;
            if (originalArg instanceof QName) {
                final QName originalQName = (QName) originalArg;
                this.argument = (A) getFromNamespace(QNameCacheNamespace.class,
                        QName.create(newQNameModule, originalQName.getLocalName()));
            } else if (StmtContextUtils.producesDeclared(original, KeyStatement.class)) {
                this.argument = (A) StmtContextUtils.replaceModuleQNameForKey(
                        (StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?>) original, newQNameModule);
            } else {
                this.argument = original.argument;
            }
        } else {
            this.argument = original.argument;
        }
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

    @Override
    public StatementContextBase<A, D, E> createCopy(final StatementContextBase<?, ?, ?> newParent,
            final CopyType typeOfCopy) {
        return createCopy(null, newParent, typeOfCopy);
    }

    @Override
    public StatementContextBase<A, D, E> createCopy(final QNameModule newQNameModule,
            final StatementContextBase<?, ?, ?> newParent, final CopyType typeOfCopy) {
        Preconditions.checkState(getCompletedPhase() == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Attempted to copy statement %s which has completed phase %s", this, getCompletedPhase());

        final SubstatementContext<A, D, E> copy = new SubstatementContext<>(this, newQNameModule, newParent, typeOfCopy);

        definition().onStatementAdded(copy);

        copy.copyStatements(this, newQNameModule, typeOfCopy);
        return copy;
    }

    private void copyStatements(final SubstatementContext<A, D, E> original, final QNameModule newQNameModule,
            final CopyType typeOfCopy) {
        final Collection<? extends Mutable<?, ?, ?>> declared = original.mutableDeclaredSubstatements();
        final Collection<? extends Mutable<?, ?, ?>> effective = original.mutableEffectiveSubstatements();
        final Collection<Mutable<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());

        for (final Mutable<?, ?, ?> stmtContext : declared) {
            if (stmtContext.isSupportedByFeatures()) {
                copySubstatement(stmtContext, newQNameModule, typeOfCopy, buffer);
            }
        }

        for (final Mutable<?, ?, ?> stmtContext : effective) {
            copySubstatement(stmtContext, newQNameModule, typeOfCopy, buffer);
        }

        addEffectiveSubstatements(buffer);
    }

    private void copySubstatement(final Mutable<?, ?, ?> stmtContext, final QNameModule newQNameModule,
            final CopyType typeOfCopy, final Collection<Mutable<?, ?, ?>> buffer) {
        if (needToCopyByUses(stmtContext)) {
            final Mutable<?, ?, ?> copy = stmtContext.createCopy(newQNameModule, this, typeOfCopy);
            LOG.debug("Copying substatement {} for {} as", stmtContext, this, copy);
            buffer.add(copy);
        } else if (isReusedByUses(stmtContext)) {
            LOG.debug("Reusing substatement {} for {}", stmtContext, this);
            buffer.add(stmtContext);
        } else {
            LOG.debug("Skipping statement {}", stmtContext);
        }
    }

    // FIXME: revise this, as it seems to be wrong
    private static final Set<YangStmtMapping> NOCOPY_FROM_GROUPING_SET = ImmutableSet.of(
        YangStmtMapping.DESCRIPTION,
        YangStmtMapping.REFERENCE,
        YangStmtMapping.STATUS);
    private static final Set<YangStmtMapping> REUSED_DEF_SET = ImmutableSet.of(
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
            return !YangStmtMapping.GROUPING.equals(stmtContext.getParentContext().getPublicDefinition());
        }

        LOG.debug("Will copy {} statement {}", def, stmtContext);
        return true;
    }

    private static boolean isReusedByUses(final StmtContext<?, ?, ?> stmtContext) {
        return REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
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
        // if this statement is within a 'yang-data' extension body, config substatements are ignored as if
        // they were not declared. As 'yang-data' is always a top-level node, all configs that are within it are
        // automatically true
        if (isInYangDataExtensionBody()) {
            return true;
        }

        if (haveConfiguration) {
            return configuration;
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
        configuration = isConfig;
        haveConfiguration = true;
        return isConfig;
    }

    @Override
    public boolean isInYangDataExtensionBody() {
        if (wasCheckedIfInYangDataExtensionBody) {
            return isInYangDataExtensionBody;
        }

        final boolean parentIsInYangDataExtensionBody = parent.isInYangDataExtensionBody();
        if (parentIsInYangDataExtensionBody) {
            isInYangDataExtensionBody = parentIsInYangDataExtensionBody;
        } else {
            isInYangDataExtensionBody = StmtContextUtils.hasYangDataExtensionParent(this);
        }

        wasCheckedIfInYangDataExtensionBody = true;
        return isInYangDataExtensionBody;
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
}
