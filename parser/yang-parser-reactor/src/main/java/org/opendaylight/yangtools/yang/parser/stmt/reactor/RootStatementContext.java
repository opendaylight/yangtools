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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.RootStmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root statement class for a YANG source. All statements defined in that YANG source are mapped underneath an instance
 * of this class, hence recursive lookups from them cross this class.
 */
public final class RootStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends AbstractResumedStatement<A, D, E> implements RootStmtContext.Mutable<A, D, E> {
    public static final YangVersion DEFAULT_VERSION = YangVersion.VERSION_1;

    private static final Logger LOG = LoggerFactory.getLogger(RootStatementContext.class);
    // These namespaces are well-known and not needed after the root is cleaned up
    private static final Map<Class<?>, SweptNamespace> SWEPT_NAMESPACES = ImmutableMap.of(
        GroupingNamespace.class, new SweptNamespace(GroupingNamespace.class),
        SchemaTreeNamespace.class, new SweptNamespace(SchemaTreeNamespace.class),
        TypeNamespace.class, new SweptNamespace(TypeNamespace.class));

    private final @NonNull SourceSpecificContext sourceContext;
    private final A argument;

    private YangVersion rootVersion;
    private Set<SourceIdentifier> requiredSources = ImmutableSet.of();
    private SourceIdentifier rootIdentifier;

    /**
     * References to RootStatementContext of submodules which are included in this source.
     */
    private List<RootStatementContext<?, ?, ?>> includedContexts = ImmutableList.of();

    RootStatementContext(final SourceSpecificContext sourceContext, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument) {
        super(def, ref, rawArgument);
        this.sourceContext = requireNonNull(sourceContext);
        this.argument = def.parseArgumentValue(this, rawArgument());
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
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.ROOT_STATEMENT_LOCAL;
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        // this as its own root
        return this;
    }

    SourceSpecificContext getSourceContext() {
        return sourceContext;
    }

    @Override
    public A argument() {
        return argument;
    }

    @Override
    @Deprecated
    public SchemaPath schemaPath() {
        return SchemaPath.ROOT;
    }

    @Override
    public EffectiveConfig effectiveConfig() {
        return EffectiveConfig.UNDETERMINED;
    }

    @Override
    public <K, V, N extends ParserNamespace<K, V>> V putToLocalStorage(final Class<N> type, final K key,
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

    @Override
    public <K, V, N extends ParserNamespace<K, V>> V getFromLocalStorage(final Class<N> type, final K key) {
        return getFromLocalStorage(type, key, new HashSet<>());
    }

    /*
     * We need to track already checked RootStatementContexts due to possible
     * circular chains of includes between submodules
     */
    private <K, V, N extends ParserNamespace<K, V>> @Nullable V getFromLocalStorage(final Class<N> type,
            final K key, final HashSet<RootStatementContext<?, ?, ?>> alreadyChecked) {
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

    @Override
    public <K, V, N extends ParserNamespace<K, V>> Map<K, V> getAllFromLocalStorage(final Class<N> type) {
        return getAllFromLocalStorage(type, new HashSet<>());
    }

    /*
     * We need to track already checked RootStatementContexts due to possible
     * circular chains of includes between submodules
     */
    private <K, V, N extends ParserNamespace<K, V>> @Nullable Map<K, V> getAllFromLocalStorage(final Class<N> type,
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

    /**
     * Return the set of required sources.
     *
     * @return Required sources.
     */
    Collection<SourceIdentifier> getRequiredSources() {
        return ImmutableSet.copyOf(requiredSources);
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

    void setRootIdentifierImpl(final SourceIdentifier identifier) {
        this.rootIdentifier = requireNonNull(identifier);
    }

    @NonNull Registry getBehaviourRegistryImpl() {
        return sourceContext;
    }

    @NonNull YangVersion getRootVersionImpl() {
        return rootVersion == null ? DEFAULT_VERSION : rootVersion;
    }

    void setRootVersionImpl(final YangVersion version) {
        checkArgument(sourceContext.globalContext().getSupportedVersions().contains(version),
                "Unsupported yang version %s in %s", version, sourceReference());
        checkState(this.rootVersion == null, "Version of root %s has been already set to %s", argument,
                this.rootVersion);
        this.rootVersion = requireNonNull(version);
    }

    /**
     * Add mutable statement to seal. Each mutable statement must be sealed
     * as the last step of statement parser processing.
     *
     * @param mutableStatement
     *            mutable statement which should be sealed
     */
    void addMutableStmtToSeal(final MutableStatement mutableStatement) {
        sourceContext.globalContext().addMutableStmtToSeal(mutableStatement);
    }

    void addRequiredSourceImpl(final SourceIdentifier dependency) {
        checkState(sourceContext.getInProgressPhase() == ModelProcessingPhase.SOURCE_PRE_LINKAGE,
                "Add required module is allowed only in ModelProcessingPhase.SOURCE_PRE_LINKAGE phase");
        if (requiredSources.isEmpty()) {
            requiredSources = new HashSet<>();
        }
        requiredSources.add(dependency);
    }

    @Override
    StatementContextBase<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        throw new UnsupportedOperationException("Root statement cannot be reparented to " + newParent);
    }

    @Override
    void sweepNamespaces() {
        LOG.trace("Sweeping root {}", this);
        sweepNamespaces(SWEPT_NAMESPACES);
    }
}
