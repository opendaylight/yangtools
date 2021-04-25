/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToModuleCtx;

final class ModuleEffectiveStatementImpl extends AbstractEffectiveModule<ModuleStatement, ModuleEffectiveStatement>
        implements Module, ModuleEffectiveStatement {
    private final ImmutableMap<String, SubmoduleEffectiveStatement> nameToSubmodule;
    private final ImmutableMap<QName, ExtensionEffectiveStatement> qnameToExtension;
    private final ImmutableMap<QName, FeatureEffectiveStatement> qnameToFeature;
    private final ImmutableMap<QName, IdentityEffectiveStatement> qnameToIdentity;
    private final ImmutableMap<String, ModuleEffectiveStatement> prefixToModule;
    private final ImmutableMap<QNameModule, String> namespaceToPrefix;
    private final @NonNull QNameModule qnameModule;
    private final ImmutableList<Submodule> submodules;

    ModuleEffectiveStatementImpl(final Current<UnqualifiedQName, ModuleStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final Collection<? extends Submodule> submodules, final QNameModule qnameModule) {
        super(stmt, substatements, findPrefix(stmt, substatements, "module", stmt.getRawArgument()));
        this.qnameModule = requireNonNull(qnameModule);
        this.submodules = ImmutableList.copyOf(submodules);

        final String localPrefix = findFirstEffectiveSubstatementArgument(PrefixEffectiveStatement.class).get();
        final Builder<String, ModuleEffectiveStatement> prefixToModuleBuilder = ImmutableMap.builder();
        prefixToModuleBuilder.put(localPrefix, this);
        appendPrefixes(stmt, prefixToModuleBuilder);
        prefixToModule = prefixToModuleBuilder.build();

        final Map<QNameModule, String> tmp = Maps.newLinkedHashMapWithExpectedSize(prefixToModule.size() + 1);
        tmp.put(qnameModule, localPrefix);
        for (Entry<String, ModuleEffectiveStatement> e : prefixToModule.entrySet()) {
            tmp.putIfAbsent(e.getValue().localQNameModule(), e.getKey());
        }
        namespaceToPrefix = ImmutableMap.copyOf(tmp);

        final Map<String, StmtContext<?, ?, ?>> includedSubmodules =
                stmt.localNamespacePortion(IncludedSubmoduleNameToModuleCtx.class);
        nameToSubmodule = includedSubmodules == null ? ImmutableMap.of()
                : ImmutableMap.copyOf(Maps.transformValues(includedSubmodules,
                    submodule -> (SubmoduleEffectiveStatement) submodule.buildEffective()));

        qnameToExtension = substatements.stream()
            .filter(ExtensionEffectiveStatement.class::isInstance)
            .map(ExtensionEffectiveStatement.class::cast)
            .collect(ImmutableMap.toImmutableMap(ExtensionEffectiveStatement::argument, Function.identity()));
        qnameToFeature = substatements.stream()
            .filter(FeatureEffectiveStatement.class::isInstance)
            .map(FeatureEffectiveStatement.class::cast)
            .collect(ImmutableMap.toImmutableMap(FeatureEffectiveStatement::argument, Function.identity()));
        qnameToIdentity = substatements.stream()
            .filter(IdentityEffectiveStatement.class::isInstance)
            .map(IdentityEffectiveStatement.class::cast)
            .collect(ImmutableMap.toImmutableMap(IdentityEffectiveStatement::argument, Function.identity()));
    }

    @Override
    public QNameModule localQNameModule() {
        return qnameModule;
    }

    @Override
    public QNameModule getQNameModule() {
        return qnameModule;
    }

    @Override
    public Collection<? extends @NonNull Submodule> getSubmodules() {
        return submodules;
    }

    @Override
    public ModuleEffectiveStatement asEffectiveStatement() {
        return this;
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
        if (NameToEffectiveSubmoduleNamespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) nameToSubmodule);
        }
        if (ExtensionEffectiveStatementNamespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) qnameToExtension);
        }
        if (FeatureEffectiveStatementNamespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) qnameToFeature);
        }
        if (IdentityEffectiveStatementNamespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) qnameToIdentity);
        }
        return super.getNamespaceContents(namespace);
    }
}
