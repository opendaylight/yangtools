/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.PrefixToEffectiveModuleNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.QNameModuleToPrefixNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class SubmoduleEffectiveStatementImpl
        extends AbstractEffectiveModule<SubmoduleStatement, SubmoduleEffectiveStatement>
        implements Submodule, SubmoduleEffectiveStatement, MutableStatement {
    private final ImmutableMap<String, ModuleEffectiveStatement> prefixToModule;
    private final ImmutableMap<QNameModule, String> namespaceToPrefix;
    private final QNameModule qnameModule;

    private Set<StmtContext<?, SubmoduleStatement, SubmoduleEffectiveStatement>> submoduleContexts;
    private ImmutableSet<Submodule> submodules;
    private boolean sealed;

    SubmoduleEffectiveStatementImpl(final Current<UnqualifiedQName, SubmoduleStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements, findSubmodulePrefix(stmt, substatements));

        final QNameModule belongsToModuleQName = stmt.getFromNamespace(ModuleNameToModuleQName.class,
            findBelongsTo(stmt, substatements).argument());

        final Builder<String, ModuleEffectiveStatement> prefixToModuleBuilder = ImmutableMap.builder();
        appendPrefixes(stmt, prefixToModuleBuilder);
        prefixToModule = prefixToModuleBuilder.build();

        final Map<QNameModule, String> tmp = Maps.newLinkedHashMapWithExpectedSize(prefixToModule.size());
        for (Entry<String, ModuleEffectiveStatement> e : prefixToModule.entrySet()) {
            tmp.putIfAbsent(e.getValue().localQNameModule(), e.getKey());
        }
        namespaceToPrefix = ImmutableMap.copyOf(tmp);

        final Optional<Revision> submoduleRevision = findFirstEffectiveSubstatementArgument(
            RevisionEffectiveStatement.class);
        this.qnameModule = QNameModule.create(belongsToModuleQName.getNamespace(), submoduleRevision).intern();

        /*
         * Because of possible circular chains of includes between submodules we can
         * collect only submodule contexts here and then build them during
         * sealing of this statement.
         */
        final Map<String, StmtContext<?, ?, ?>> includedSubmodulesMap = stmt.localNamespacePortion(
            IncludedSubmoduleNameToModuleCtx.class);
        if (includedSubmodulesMap != null) {
            final Set<StmtContext<?, SubmoduleStatement, SubmoduleEffectiveStatement>> submoduleContextsInit =
                new HashSet<>();
            for (final StmtContext<?, ?, ?> submoduleCtx : includedSubmodulesMap.values()) {
                submoduleContextsInit.add(
                    (StmtContext<?, SubmoduleStatement, SubmoduleEffectiveStatement>)submoduleCtx);
            }
            submoduleContexts = ImmutableSet.copyOf(submoduleContextsInit);
        } else {
            submoduleContexts = ImmutableSet.of();
        }

        if (!submoduleContexts.isEmpty()) {
            ((Mutable<?, ?, ?>) stmt.caerbannog()).addMutableStmtToSeal(this);
            sealed = false;
        } else {
            submodules = ImmutableSet.of();
            sealed = true;
        }
    }

    @Override
    public QNameModule getQNameModule() {
        return qnameModule;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final @NonNull Class<N> namespace) {
        if (PrefixToEffectiveModuleNamespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) prefixToModule);
        }
        if (QNameModuleToPrefixNamespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) namespaceToPrefix);
        }
        return super.getNamespaceContents(namespace);
    }

    @Override
    public Collection<? extends @NonNull Submodule> getSubmodules() {
        checkState(sealed, "Attempt to get base submodules from unsealed submodule effective statement %s",
            qnameModule);
        return submodules;
    }

    @Override
    public SubmoduleEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public void seal() {
        if (!sealed) {
            submodules = ImmutableSet.copyOf(Iterables.transform(submoduleContexts,
                ctx -> (Submodule) ctx.buildEffective()));
            submoduleContexts = ImmutableSet.of();
            sealed = true;
        }
    }

    private static @NonNull BelongsToEffectiveStatement findBelongsTo(final CommonStmtCtx stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.stream()
            .filter(BelongsToEffectiveStatement.class::isInstance)
            .map(BelongsToEffectiveStatement.class::cast)
            .findAny().orElseThrow(() -> new SourceException(stmt,
                "Unable to find belongs-to statement in submodule %s.", stmt.rawArgument()));
    }

    private static @NonNull String findSubmodulePrefix(final CommonStmtCtx stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return findPrefix(stmt, findBelongsTo(stmt, substatements).effectiveSubstatements(), "submodule",
            stmt.getRawArgument());
    }
}
