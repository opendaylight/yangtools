/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveModule;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.FeatureNamespace;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

final class ModuleEffectiveStatementImpl extends AbstractEffectiveModule<ModuleStatement>
        implements ModuleEffectiveStatement {
    private final ImmutableMap<String, SubmoduleEffectiveStatement> nameToSubmodule;
    private final ImmutableMap<QName, ExtensionEffectiveStatement> qnameToExtension;
    private final ImmutableMap<QName, FeatureEffectiveStatement> qnameToFeature;
    private final ImmutableMap<QName, IdentityEffectiveStatement> qnameToIdentity;
    private final ImmutableMap<String, ModuleEffectiveStatement> prefixToModule;
    private final ImmutableMap<QNameModule, String> namespaceToPrefix;
    private final @NonNull QNameModule qnameModule;

    ModuleEffectiveStatementImpl(final StmtContext<String, ModuleStatement, ModuleEffectiveStatement> ctx) {
        super(ctx);
        qnameModule = verifyNotNull(ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx));

        final String localPrefix = findFirstEffectiveSubstatementArgument(PrefixEffectiveStatement.class).get();
        final Builder<String, ModuleEffectiveStatement> prefixToModuleBuilder = ImmutableMap.builder();
        prefixToModuleBuilder.put(localPrefix, this);

        streamEffectiveSubstatements(ImportEffectiveStatement.class)
                .map(imp -> imp.findFirstEffectiveSubstatementArgument(PrefixEffectiveStatement.class).get())
                .forEach(prefix -> {
                    final StmtContext<?, ?, ?> importedCtx =
                            verifyNotNull(ctx.getFromNamespace(ImportPrefixToModuleCtx.class, prefix),
                                "Failed to resolve prefix %s", prefix);
                    prefixToModuleBuilder.put(prefix, (ModuleEffectiveStatement) importedCtx.buildEffective());
                });
        prefixToModule = prefixToModuleBuilder.build();

        final Map<QNameModule, String> tmp = Maps.newLinkedHashMapWithExpectedSize(prefixToModule.size() + 1);
        tmp.put(qnameModule, localPrefix);
        for (Entry<String, ModuleEffectiveStatement> e : prefixToModule.entrySet()) {
            tmp.putIfAbsent(e.getValue().localQNameModule(), e.getKey());
        }
        namespaceToPrefix = ImmutableMap.copyOf(tmp);

        final Map<String, StmtContext<?, ?, ?>> submodules =
                ctx.getAllFromCurrentStmtCtxNamespace(IncludedSubmoduleNameToModuleCtx.class);
        nameToSubmodule = submodules == null ? ImmutableMap.of()
                : ImmutableMap.copyOf(Maps.transformValues(submodules,
                    submodule -> (SubmoduleEffectiveStatement) submodule.buildEffective()));

        final Map<QName, StmtContext<?, ExtensionStatement, ExtensionEffectiveStatement>> extensions =
                ctx.getAllFromCurrentStmtCtxNamespace(ExtensionNamespace.class);
        qnameToExtension = extensions == null ? ImmutableMap.of()
                : ImmutableMap.copyOf(Maps.transformValues(extensions, StmtContext::buildEffective));
        final Map<QName, StmtContext<?, FeatureStatement, FeatureEffectiveStatement>> features =
                ctx.getAllFromCurrentStmtCtxNamespace(FeatureNamespace.class);
        qnameToFeature = features == null ? ImmutableMap.of()
                : ImmutableMap.copyOf(Maps.transformValues(features, StmtContext::buildEffective));
        final Map<QName, StmtContext<?, IdentityStatement, IdentityEffectiveStatement>> identities =
                ctx.getAllFromCurrentStmtCtxNamespace(IdentityNamespace.class);
        qnameToIdentity = identities == null ? ImmutableMap.of()
                : ImmutableMap.copyOf(Maps.transformValues(identities, StmtContext::buildEffective));
    }

    @Override
    public @NonNull QNameModule localQNameModule() {
        return qnameModule;
    }

    @Override
    public @NonNull QNameModule getQNameModule() {
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

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getYangVersion(), qnameModule);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModuleEffectiveStatementImpl)) {
            return false;
        }
        ModuleEffectiveStatementImpl other = (ModuleEffectiveStatementImpl) obj;
        return Objects.equals(getName(), other.getName()) && qnameModule.equals(other.qnameModule)
                && Objects.equals(getYangVersion(), other.getYangVersion());
    }
}
