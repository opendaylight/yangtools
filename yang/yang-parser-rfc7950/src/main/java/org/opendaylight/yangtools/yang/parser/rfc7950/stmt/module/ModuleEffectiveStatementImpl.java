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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

final class ModuleEffectiveStatementImpl extends AbstractEffectiveModule<ModuleStatement>
        implements ModuleEffectiveStatement {
    private final Map<String, ModuleEffectiveStatement> prefixToModule;
    private final Map<QNameModule, String> namespaceToPrefix;
    private final @NonNull QNameModule qnameModule;

    ModuleEffectiveStatementImpl(
            final StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        super(ctx);
        qnameModule = verifyNotNull(ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx));

        final String localPrefix = findFirstEffectiveSubstatementArgument(PrefixEffectiveStatement.class).get();
        final Builder<String, ModuleEffectiveStatement> prefixToModuleBuilder = ImmutableMap.builder();
        prefixToModuleBuilder.put(localPrefix, this);
        streamEffectiveSubstatements(ImportEffectiveStatement.class).map(ImportEffectiveStatement::argument)
            .forEach(prefix -> prefixToModuleBuilder.put(prefix, (ModuleEffectiveStatement) ctx.getFromNamespace(
                ImportPrefixToModuleCtx.class, prefix).buildEffective()));

        prefixToModule = prefixToModuleBuilder.build();

        final Map<QNameModule, String> tmp = Maps.newLinkedHashMapWithExpectedSize(prefixToModule.size() + 1);
        tmp.put(qnameModule, localPrefix);
        for (Entry<String, ModuleEffectiveStatement> e : prefixToModule.entrySet()) {
            tmp.putIfAbsent(e.getValue().localNamespace(), e.getKey());
        }
        namespaceToPrefix = ImmutableMap.copyOf(tmp);
    }

    @Override
    public @NonNull QNameModule localNamespace() {
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
        return super.getNamespaceContents(namespace);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(getYangVersion());
        result = prime * result + Objects.hashCode(qnameModule);
        return result;
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
        if (!Objects.equals(getName(), other.getName())) {
            return false;
        }
        if (!qnameModule.equals(other.qnameModule)) {
            return false;
        }
        if (!Objects.equals(getYangVersion(), other.getYangVersion())) {
            return false;
        }
        return true;
    }
}
