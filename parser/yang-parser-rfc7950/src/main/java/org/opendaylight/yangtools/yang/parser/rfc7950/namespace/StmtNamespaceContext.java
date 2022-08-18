/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.xml.namespace.NamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * A {@link NamespaceContext} implementation based on the set of imports and local module namespace.
 */
// TODO: this is a useful utility, so it may be useful to expose it either in this package, or yang.parser.spi.source
final class StmtNamespaceContext implements YangNamespaceContext {
    private static final long serialVersionUID = 1L;

    private final ImmutableBiMap<QNameModule, String> moduleToPrefix;
    private final ImmutableMap<String, QNameModule> prefixToModule;

    StmtNamespaceContext(final StmtContext<?, ?, ?> ctx) {
        // QNameModule -> prefix mappings
        final Map<QNameModule, String> qnameToPrefix = ctx.getAllFromNamespace(ModuleQNameToPrefix.class);
        moduleToPrefix = qnameToPrefix == null ? ImmutableBiMap.of() : ImmutableBiMap.copyOf(qnameToPrefix);

        // Additional mappings
        final Map<String, QNameModule> additional = new HashMap<>();
        final Map<String, StmtContext<?, ?, ?>> imports = ctx.getAllFromNamespace(ImportPrefixToModuleCtx.class);
        if (imports != null) {
            for (Entry<String, StmtContext<?, ?, ?>> entry : imports.entrySet()) {
                if (!moduleToPrefix.containsValue(entry.getKey())) {
                    QNameModule qnameModule = ctx.getFromNamespace(ModuleCtxToModuleQName.class, entry.getValue());
                    if (qnameModule == null && ctx.producesDeclared(SubmoduleStatement.class)) {
                        final Unqualified moduleName = ctx.getFromNamespace(BelongsToPrefixToModuleName.class,
                            entry.getKey());
                        qnameModule = ctx.getFromNamespace(ModuleNameToModuleQName.class, moduleName);
                    }

                    if (qnameModule != null) {
                        additional.put(entry.getKey(), qnameModule);
                    }
                }
            }
        }
        if (ctx.producesDeclared(SubmoduleStatement.class)) {
            final Map<String, Unqualified> belongsTo = ctx.getAllFromNamespace(BelongsToPrefixToModuleName.class);
            if (belongsTo != null) {
                for (Entry<String, Unqualified> entry : belongsTo.entrySet()) {
                    final QNameModule module = ctx.getFromNamespace(ModuleNameToModuleQName.class, entry.getValue());
                    if (module != null && !additional.containsKey(entry.getKey())) {
                        additional.put(entry.getKey(), module);
                    }
                }
            }
        }

        prefixToModule = ImmutableMap.copyOf(additional);
    }

    @Override
    public Optional<String> findPrefixForNamespace(final QNameModule namespace) {
        return Optional.ofNullable(moduleToPrefix.get(namespace));
    }

    @Override
    public Optional<QNameModule> findNamespaceForPrefix(final String prefix) {
        final QNameModule normal = moduleToPrefix.inverse().get(prefix);
        return normal != null ? Optional.of(normal) : Optional.ofNullable(prefixToModule.get(prefix));
    }
}
