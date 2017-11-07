/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * Root statement class for a YANG source. All statements defined in that YANG source are mapped underneath an instance
 * of this class, hence recursive lookups from them cross this class.
 */
public class RootStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends
        StatementContextBase<A, D, E> {

    public static final YangVersion DEFAULT_VERSION = YangVersion.VERSION_1;

    private final SourceSpecificContext sourceContext;
    private final A argument;

    private YangVersion rootVersion;
    private Collection<SourceIdentifier> requiredSources = ImmutableSet.of();
    private SourceIdentifier rootIdentifier;

    /**
     * References to RootStatementContext of submodules which are included in this source.
     */
    private Collection<RootStatementContext<?, ?, ?>> includedContexts = ImmutableList.of();

    RootStatementContext(final SourceSpecificContext sourceContext, final StatementDefinitionContext<A, D, E> def,
        final StatementSourceReference ref, final String rawArgument) {
        super(def, ref, rawArgument);
        this.sourceContext = requireNonNull(sourceContext);
        this.argument = def.parseArgumentValue(this, rawStatementArgument());
    }

    RootStatementContext(final SourceSpecificContext sourceContext, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument, final YangVersion version,
            final SourceIdentifier identifier) {
        this(sourceContext, def, ref, rawArgument);
        this.setRootVersion(version);
        this.setRootIdentifier(identifier);
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        // null as root cannot have parent
        return null;
    }

    @Override
    public NamespaceStorageNode getParentNamespaceStorage() {
        // namespace storage of source context
        return sourceContext;
    }

    @Override
    public Registry getBehaviourRegistry() {
        return sourceContext;
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.ROOT_STATEMENT_LOCAL;
    }

    @Nonnull
    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        // this as its own root
        return this;
    }

    SourceSpecificContext getSourceContext() {
        return sourceContext;
    }

    @Override
    public A getStatementArgument() {
        return argument;
    }

    @Nonnull
    @Override
    public Optional<SchemaPath> getSchemaPath() {
        return Optional.of(SchemaPath.ROOT);
    }

    @Override
    public boolean isConfiguration() {
        return true;
    }

    @Override
    public boolean isEnabledSemanticVersioning() {
        return sourceContext.isEnabledSemanticVersioning();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V putToLocalStorage(final Class<N> type, final K key,
            final V value) {
        if (IncludedModuleContext.class.isAssignableFrom(type)) {
            if (includedContexts.isEmpty()) {
                includedContexts = new ArrayList<>(1);
            }
            verify(value instanceof RootStatementContext);
            includedContexts.add((RootStatementContext<?, ?, ?>) value);
        }
        return super.putToLocalStorage(type, key, value);
    }

    @Nullable
    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(final Class<N> type, final K key) {
        return getFromLocalStorage(type, key, new HashSet<>());
    }

    /*
     * We need to track already checked RootStatementContexts due to possible
     * circular chains of includes between submodules
     */
    @Nullable
    private <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(final Class<N> type, final K key,
            final HashSet<RootStatementContext<?, ?, ?>> alreadyChecked) {
        final V potentialLocal = super.getFromLocalStorage(type, key);
        if (potentialLocal != null) {
            return potentialLocal;
        }

        alreadyChecked.add(this);
        for (final RootStatementContext<?, ?, ?> includedSource : includedContexts) {
            if (alreadyChecked.contains(includedSource)) {
                continue;
            }
            final V potential = includedSource.getFromLocalStorage(type, key, alreadyChecked);
            if (potential != null) {
                return potential;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromLocalStorage(final Class<N> type) {
        return getAllFromLocalStorage(type, new HashSet<>());
    }

    /*
     * We need to track already checked RootStatementContexts due to possible
     * circular chains of includes between submodules
     */
    @Nullable
    private <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromLocalStorage(final Class<N> type,
            final HashSet<RootStatementContext<?, ?, ?>> alreadyChecked) {
        final Map<K, V> potentialLocal = super.getAllFromLocalStorage(type);
        if (potentialLocal != null) {
            return potentialLocal;
        }

        alreadyChecked.add(this);
        for (final RootStatementContext<?, ?, ?> includedSource : includedContexts) {
            if (alreadyChecked.contains(includedSource)) {
                continue;
            }
            final Map<K, V> potential = includedSource.getAllFromLocalStorage(type, alreadyChecked);
            if (potential != null) {
                return potential;
            }
        }
        return null;
    }

    @Override
    public YangVersion getRootVersion() {
        return rootVersion == null ? DEFAULT_VERSION : rootVersion;
    }

    @Override
    public void setRootVersion(final YangVersion version) {
        checkArgument(sourceContext.getSupportedVersions().contains(version),
                "Unsupported yang version %s in %s", version, getStatementSourceReference());
        checkState(this.rootVersion == null, "Version of root %s has been already set to %s", argument,
                this.rootVersion);
        this.rootVersion = requireNonNull(version);
    }

    @Override
    public void addMutableStmtToSeal(final MutableStatement mutableStatement) {
        sourceContext.addMutableStmtToSeal(mutableStatement);
    }

    @Override
    public void addRequiredSource(final SourceIdentifier dependency) {
        checkState(sourceContext.getInProgressPhase() == ModelProcessingPhase.SOURCE_PRE_LINKAGE,
                "Add required module is allowed only in ModelProcessingPhase.SOURCE_PRE_LINKAGE phase");
        if (requiredSources.isEmpty()) {
            requiredSources = new HashSet<>();
        }
        requiredSources.add(dependency);
    }

    /**
     * Return the set of required sources.
     *
     * @return Required sources.
     */
    Collection<SourceIdentifier> getRequiredSources() {
        return ImmutableSet.copyOf(requiredSources);
    }

    @Override
    public void setRootIdentifier(final SourceIdentifier identifier) {
        this.rootIdentifier = requireNonNull(identifier);
    }

    SourceIdentifier getRootIdentifier() {
        return rootIdentifier;
    }

    @Override
    protected boolean isIgnoringIfFeatures() {
        return false;
    }

    @Override
    protected boolean isIgnoringConfig() {
        return false;
    }

    @Override
    protected boolean isParentSupportedByFeatures() {
        return true;
    }
}
