/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import javax.xml.namespace.NamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
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
        final var qnameToPrefix = ctx.namespace(ModuleQNameToPrefix.INSTANCE);
        moduleToPrefix = qnameToPrefix == null ? ImmutableBiMap.of() : ImmutableBiMap.copyOf(qnameToPrefix);

        // Additional mappings
        final var additional = new HashMap<String, QNameModule>();
        final var imports = ctx.namespace(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX);
        if (imports != null) {
            for (var entry : imports.entrySet()) {
                if (!moduleToPrefix.containsValue(entry.getKey())) {
                    var qnameModule = ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, entry.getValue());
                    if (qnameModule == null && ctx.producesDeclared(SubmoduleStatement.class)) {
                        qnameModule = ctx.namespaceItem(ParserNamespaces.MODULE_NAME_TO_QNAME,
                            ctx.namespaceItem(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME, entry.getKey()));
                    }

                    if (qnameModule != null) {
                        additional.put(entry.getKey(), qnameModule);
                    }
                }
            }
        }
        if (ctx.producesDeclared(SubmoduleStatement.class)) {
            final var belongsTo = ctx.namespace(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME);
            if (belongsTo != null) {
                for (var entry : belongsTo.entrySet()) {
                    final var module = ctx.namespaceItem(ParserNamespaces.MODULE_NAME_TO_QNAME, entry.getValue());
                    if (module != null && !additional.containsKey(entry.getKey())) {
                        additional.put(entry.getKey(), module);
                    }
                }
            }
        }

        prefixToModule = ImmutableMap.copyOf(additional);
    }

    @Override
    public String prefixForNamespace(final QNameModule namespace) {
        return moduleToPrefix.get(requireNonNull(namespace));
    }

    @Override
    public QNameModule namespaceForPrefix(final String prefix) {
        final var checked = requireNonNull(prefix);
        final var normal = moduleToPrefix.inverse().get(checked);
        return normal != null ? normal : prefixToModule.get(checked);
    }
}
