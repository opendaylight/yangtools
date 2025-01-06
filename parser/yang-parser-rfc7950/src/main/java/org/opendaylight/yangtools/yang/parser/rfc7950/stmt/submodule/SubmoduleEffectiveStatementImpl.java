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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveModule;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class SubmoduleEffectiveStatementImpl
        extends AbstractEffectiveModule<SubmoduleStatement, SubmoduleEffectiveStatement>
        implements Submodule, SubmoduleEffectiveStatement, MutableStatement {
    private final ImmutableMap<String, ModuleEffectiveStatement> prefixToModule;
    private final ImmutableMap<QNameModule, String> namespaceToPrefix;
    private final QNameModule qnameModule;

    private ImmutableSet<StmtContext<?, SubmoduleStatement, SubmoduleEffectiveStatement>> submoduleContexts;
    private ImmutableSet<Submodule> submodules;
    private boolean sealed;

    SubmoduleEffectiveStatementImpl(final Current<Unqualified, SubmoduleStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements, findSubmodulePrefix(stmt, substatements));

        final QNameModule belongsToModuleQName = stmt.namespaceItem(ParserNamespaces.MODULE_NAME_TO_QNAME,
            findBelongsTo(stmt, substatements).argument());

        final var prefixToModuleBuilder = ImmutableMap.<String, ModuleEffectiveStatement>builder();
        appendPrefixes(stmt, prefixToModuleBuilder);
        prefixToModule = prefixToModuleBuilder.build();

        final var tmp = LinkedHashMap.<QNameModule, String>newLinkedHashMap(prefixToModule.size());
        for (var e : prefixToModule.entrySet()) {
            tmp.putIfAbsent(e.getValue().localQNameModule(), e.getKey());
        }
        namespaceToPrefix = ImmutableMap.copyOf(tmp);

        final var submoduleRevision = findFirstEffectiveSubstatementArgument(RevisionEffectiveStatement.class);
        qnameModule = QNameModule.ofRevision(belongsToModuleQName.namespace(), submoduleRevision.orElse(null))
            .intern();

        /*
         * Because of possible circular chains of includes between submodules we can
         * collect only submodule contexts here and then build them during
         * sealing of this statement.
         */
        final var includedSubmodulesMap = stmt.localNamespacePortion(
            ParserNamespaces.INCLUDED_SUBMODULE_NAME_TO_MODULECTX);
        if (includedSubmodulesMap != null) {
            final var submoduleContextsInit =
                new HashSet<StmtContext<?, SubmoduleStatement, SubmoduleEffectiveStatement>>();
            for (final StmtContext<?, ?, ?> submoduleCtx : includedSubmodulesMap.values()) {
                submoduleContextsInit.add(
                    (StmtContext<?, SubmoduleStatement, SubmoduleEffectiveStatement>)submoduleCtx);
            }
            submoduleContexts = ImmutableSet.copyOf(submoduleContextsInit);
        } else {
            submoduleContexts = ImmutableSet.of();
        }

        if (submoduleContexts.isEmpty()) {
            submodules = ImmutableSet.of();
            sealed = true;
        } else {
            sealed = false;
        }
    }

    @Override
    public QNameModule getQNameModule() {
        return qnameModule;
    }

    @Override
    public Collection<Entry<String, ModuleEffectiveStatement>> reachableModules() {
        return prefixToModule.entrySet();
    }

    @Override
    public Optional<ModuleEffectiveStatement> findReachableModule(final String prefix) {
        return findValue(prefixToModule, prefix);
    }

    @Override
    public Collection<Entry<QNameModule, String>> namespacePrefixes() {
        return namespaceToPrefix.entrySet();
    }

    @Override
    public Optional<String> findNamespacePrefix(final QNameModule namespace) {
        return findValue(namespaceToPrefix, namespace);
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
