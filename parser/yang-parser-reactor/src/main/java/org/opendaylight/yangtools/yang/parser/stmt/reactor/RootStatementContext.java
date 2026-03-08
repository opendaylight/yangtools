/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.IdentifierBinding;
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
        extends AbstractResumedStatement<A, D, E>
        implements RootStmtContext.Mutable<A, D, E>, NamespaceStorage.Source {
    private static final Logger LOG = LoggerFactory.getLogger(RootStatementContext.class);
    // These namespaces are well-known and not needed after the root is cleaned up
    private static final Map<ParserNamespace<?, ?>, SweptNamespace> SWEPT_NAMESPACES = ImmutableMap.of(
        ParserNamespaces.GROUPING, new SweptNamespace(ParserNamespaces.GROUPING),
        ParserNamespaces.schemaTree(), new SweptNamespace(ParserNamespaces.schemaTree()),
        ParserNamespaces.TYPE, new SweptNamespace(ParserNamespaces.TYPE));

    private final @NonNull IdentifierBinding identifierBinding;
    private final @NonNull SourceSpecificContext sourceContext;
    private final @NonNull QNameModule currentModule;
    private final @NonNull Unqualified sourceName;
    private final A argument;

    /**
     * References to RootStatementContext of submodules which are included in this source.
     */
    private Set<RootStatementContext<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement>> includedSubmodules;

    RootStatementContext(final Unqualified sourceName, final IdentifierBinding identifierBinding,
            final SourceSpecificContext sourceContext, final StatementDefinitionContext<A, D, E> def,
            final StatementSourceReference ref, final String rawArgument, final int expectedSize) {
        super(def, ref, rawArgument);
        this.sourceName = requireNonNull(sourceName);
        this.identifierBinding = requireNonNull(identifierBinding);
        this.sourceContext = requireNonNull(sourceContext);
        // cache for frequent access
        currentModule = identifierBinding.namespaceBinding().currentModule();

        argument = def.argumentFactory().parseArgumentValue(this, rawArgument());
        if (!sourceName.equals(argument)) {
            throw new VerifyException("argument mismatch, expected " + sourceName + ", parsed " + argument);
        }

        resizeSubstatements(expectedSize);
    }

    void setIncludedSubmodules(
            final Collection<RootStatementContext<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement>> coll) {
        if (includedSubmodules != null) {
            throw new VerifyException("cannot replace " + includedSubmodules);
        }
        includedSubmodules = Set.copyOf(coll);
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        // null as root cannot have parent
        return null;
    }

    @Override
    public NamespaceStorage.Level level() {
        return NamespaceStorage.Level.SOURCE;
    }

    @Override
    public NamespaceStorage getParentStorage() {
        // namespace storage of source context
        return sourceContext;
    }

    @Override
    public QNameModule currentModule() {
        return currentModule;
    }

    @Override
    public Unqualified sourceName() {
        return sourceName;
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        // this as its own root
        return this;
    }

    @Override
    public IdentifierBinding identifierBinding() {
        return identifierBinding;
    }

    /**
     * Return the {@link QNameModule} corresponding to a prefix The lookup consults {@code import} and
     * {@code belongs-to} statements.
     *
     * @param prefix the prefix
     * @return the {@link QNameModule}, or {@code null} if not found
     */
    @Nullable QNameModule getModuleQNameByPrefix(final String prefix) {
        final var unqualified = Unqualified.tryLocalName(prefix);
        return unqualified == null ? null : identifierBinding.namespaceBinding().lookupModule(unqualified);
    }

    @NonNull SourceSpecificContext getSourceContext() {
        return sourceContext;
    }

    @Override
    public A argument() {
        return argument;
    }

    @Override
    public Boolean effectiveConfig() {
        return null;
    }

    @Override
    public QNameModule effectiveNamespace() {
        return currentModule();
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
        final var potentialLocal = super.getFromLocalStorage(type, key);
        if (potentialLocal != null) {
            return potentialLocal;
        }

        alreadyChecked.add(this);
        for (var includedSubmodule : includedSubmodules) {
            if (alreadyChecked.contains(includedSubmodule)) {
                continue;
            }
            final var potential = includedSubmodule.getFromLocalStorage(type, key, alreadyChecked);
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
     * We need to track already checked RootStatementContexts due to possible circular chains of includes between
     * submodules
     */
    private <K, V> @Nullable Map<K, V> getAllFromLocalStorage(final ParserNamespace<K, V> type,
            final HashSet<RootStatementContext<?, ?, ?>> alreadyChecked) {
        final var potentialLocal = super.getAllFromLocalStorage(type);
        if (potentialLocal != null) {
            return potentialLocal;
        }

        alreadyChecked.add(this);
        for (var includedSubmodule : includedSubmodules) {
            if (alreadyChecked.contains(includedSubmodule)) {
                continue;
            }
            final var potential = includedSubmodule.getAllFromLocalStorage(type, alreadyChecked);
            if (potential != null) {
                return potential;
            }
        }
        return null;
    }

    @Override
    protected boolean isParentSupportedByFeatures() {
        return true;
    }

    @NonNull YangVersion getRootVersionImpl() {
        return sourceContext.yangVersion();
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
