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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveModule;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class ModuleEffectiveStatementImpl extends AbstractEffectiveModule<ModuleStatement, ModuleEffectiveStatement>
        implements Module, ModuleEffectiveStatement {
    private final ImmutableMap<Unqualified, SubmoduleEffectiveStatement> nameToSubmodule;
    private final ImmutableMap<QName, ExtensionEffectiveStatement> qnameToExtension;
    private final ImmutableMap<QName, FeatureEffectiveStatement> qnameToFeature;
    private final ImmutableMap<QName, IdentityEffectiveStatement> qnameToIdentity;
    private final ImmutableMap<String, ModuleEffectiveStatement> prefixToModule;
    private final ImmutableMap<QNameModule, String> namespaceToPrefix;
    private final @NonNull QNameModule qnameModule;
    private final ImmutableList<Submodule> submodules;

    ModuleEffectiveStatementImpl(final Current<Unqualified, ModuleStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final Collection<? extends Submodule> submodules, final QNameModule qnameModule) {
        super(stmt, substatements, findPrefix(stmt, substatements, "module", stmt.getRawArgument()));
        this.qnameModule = requireNonNull(qnameModule);
        this.submodules = ImmutableList.copyOf(submodules);

        final String localPrefix = prefix().argument();
        final var prefixToModuleBuilder = ImmutableMap.<String, ModuleEffectiveStatement>builder();
        prefixToModuleBuilder.put(localPrefix, this);
        appendPrefixes(stmt, prefixToModuleBuilder);
        prefixToModule = prefixToModuleBuilder.build();

        final var tmp = LinkedHashMap.<QNameModule, String>newLinkedHashMap(prefixToModule.size() + 1);
        tmp.put(qnameModule, localPrefix);
        for (var e : prefixToModule.entrySet()) {
            tmp.putIfAbsent(e.getValue().localQNameModule(), e.getKey());
        }
        namespaceToPrefix = ImmutableMap.copyOf(tmp);

        final var resolvedInfo = stmt.namespaceItem(ParserNamespaces.RESOLVED_INFO, Empty.value());

        nameToSubmodule = resolvedInfo == null ? ImmutableMap.of()
            : ImmutableMap.copyOf(resolvedInfo.includes()
            .stream()
            .map(include -> Map.entry(
                include.includeId().name(),
                (SubmoduleEffectiveStatement) include.rootContext().buildEffective()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

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
    public ConformanceType conformance() {
        // FIXME: YANGTOOLS-837: provide an accurate value here
        return ConformanceType.IMPLEMENT;
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
    public Collection<ExtensionEffectiveStatement> extensions() {
        return qnameToExtension.values();
    }

    @Override
    public Optional<ExtensionEffectiveStatement> findExtension(final QName qname) {
        return findValue(qnameToExtension, qname);
    }

    @Override
    public Collection<FeatureEffectiveStatement> features() {
        return qnameToFeature.values();
    }

    @Override
    public Optional<FeatureEffectiveStatement> findFeature(final QName qname) {
        return findValue(qnameToFeature, qname);
    }

    @Override
    public Collection<IdentityEffectiveStatement> identities() {
        return qnameToIdentity.values();
    }

    @Override
    public Optional<IdentityEffectiveStatement> findIdentity(final QName qname) {
        return findValue(qnameToIdentity, qname);
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
    public Optional<String> findNamespacePrefix(final QNameModule namespace) {
        return findValue(namespaceToPrefix, namespace);
    }

    @Override
    public Collection<Entry<QNameModule, String>> namespacePrefixes() {
        return namespaceToPrefix.entrySet();
    }

    @Override
    public Collection<SubmoduleEffectiveStatement> submodules() {
        return nameToSubmodule.values();
    }

    @Override
    public Optional<SubmoduleEffectiveStatement> findSubmodule(final Unqualified submoduleName) {
        return findValue(nameToSubmodule, submoduleName);
    }
}
