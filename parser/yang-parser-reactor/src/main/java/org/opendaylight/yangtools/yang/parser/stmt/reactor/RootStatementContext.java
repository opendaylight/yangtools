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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.RootStmtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root statement class for a YANG source. All statements defined in that YANG source are mapped underneath an instance
 * of this class, hence recursive lookups from them cross this class.
 */
final class RootStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends AbstractResumedStatement<A, D, E> implements RootStmtContext.Mutable<A, D, E> {
    static final YangVersion DEFAULT_VERSION = YangVersion.VERSION_1;

    private static final Logger LOG = LoggerFactory.getLogger(RootStatementContext.class);
    // These namespaces are well-known and not needed after the root is cleaned up
    private static final Map<ParserNamespace<?, ?>, SweptNamespace> SWEPT_NAMESPACES = ImmutableMap.of(
        ParserNamespaces.GROUPING, new SweptNamespace(ParserNamespaces.GROUPING),
        ParserNamespaces.schemaTree(), new SweptNamespace(ParserNamespaces.schemaTree()),
        ParserNamespaces.TYPE, new SweptNamespace(ParserNamespaces.TYPE));

    private final @NonNull SourceSpecificContext sourceContext;
    private final A argument;

    private YangVersion rootVersion;
    @Deprecated(since = "12.0.0", forRemoval = true)
    private final Set<SourceIdentifier> requiredSources = ImmutableSet.of();
    private SourceIdentifier rootIdentifier;

    /**
     * References to RootStatementContext of submodules which are included in this source.
     */
    private List<RootStatementContext<?, ?, ?>> includedContexts = ImmutableList.of();

    RootStatementContext(final SourceSpecificContext sourceContext, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument) {
        super(def, ref, rawArgument);
        this.sourceContext = requireNonNull(sourceContext);
        argument = def.parseArgumentValue(this, rawArgument());
    }

    RootStatementContext(final SourceSpecificContext sourceContext, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument, final YangVersion version,
            final SourceIdentifier identifier) {
        this(sourceContext, def, ref, rawArgument);
        setRootVersion(version);
        setRootIdentifier(identifier);
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        // null as root cannot have parent
        return null;
    }

    @Override
    public NamespaceStorage getParentStorage() {
        // namespace storage of source context
        return sourceContext;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.ROOT_STATEMENT_LOCAL;
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        // this as its own root
        return this;
    }

    @NonNull SourceSpecificContext getSourceContext() {
        return sourceContext;
    }

    @Override
    public A argument() {
        return argument;
    }

    @Override
    public EffectiveConfig effectiveConfig() {
        return EffectiveConfig.UNDETERMINED;
    }

    @Override
    public <K, V> V putToLocalStorage(final ParserNamespace<K, V> type, final K key, final V value) {
        if (ParserNamespaces.INCLUDED_MODULE.equals(type)) {
            if (includedContexts.isEmpty()) {
                includedContexts = new ArrayList<>(1);
            }
            verify(value instanceof RootStatementContext);
            includedContexts.add((RootStatementContext<?, ?, ?>) value);
        }
        return super.putToLocalStorage(type, key, value);
    }

    @Override
    public <K, V> V getFromLocalStorage(final ParserNamespace<K, V> type, final K key) {
        return getFromLocalStorage(type, key, new HashSet<>());
    }

    /*
     * We need to track already checked RootStatementContexts due to possible
     * circular chains of includes between submodules
     */
    private <K, V> @Nullable V getFromLocalStorage(final ParserNamespace<K, V> type, final K key,
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

    @Override
    public <K, V> Map<K, V> getAllFromLocalStorage(final ParserNamespace<K, V> type) {
        return getAllFromLocalStorage(type, new HashSet<>());
    }

    /*
     * We need to track already checked RootStatementContexts due to possible
     * circular chains of includes between submodules
     */
    private <K, V> @Nullable Map<K, V> getAllFromLocalStorage(final ParserNamespace<K, V> type,
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
    @Deprecated(since = "12.0.0", forRemoval = true)
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
        rootIdentifier = requireNonNull(identifier);
    }

    @NonNull YangVersion getRootVersionImpl() {
        return rootVersion == null ? DEFAULT_VERSION : rootVersion;
    }

    void setRootVersionImpl(final YangVersion version) {
        checkArgument(sourceContext.globalContext().getSupportedVersions().contains(version),
                "Unsupported yang version %s in %s", version, sourceReference());
        checkState(rootVersion == null, "Version of root %s has been already set to %s", argument,
                rootVersion);
        rootVersion = requireNonNull(version);
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

    @Deprecated
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
