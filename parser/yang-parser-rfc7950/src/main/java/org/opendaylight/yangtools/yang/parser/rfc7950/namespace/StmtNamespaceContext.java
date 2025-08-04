/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import org.opendaylight.yangtools.yang.common.Empty;
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

    private final ImmutableBiMap<QNameModule, String> moduleToPrefix = ImmutableBiMap.of();
    private final ImmutableMap<String, QNameModule> prefixToModule;

    //TODO: revisit and consider if this class is even necessary anymore
    StmtNamespaceContext(final StmtContext<?, ?, ?> ctx) {
        // Additional mappings
        final var prefixesToQnames = new HashMap<String, QNameModule>();
        final var resolvedInfo = verifyNotNull(ctx.namespaceItem(ParserNamespaces.RESOLVED_INFO, Empty.value()));
        final Map<String, QNameModule> imports = resolvedInfo.getImportsPrefixToQNameIncludingSelf();

        for (var entry : imports.entrySet()) {
            if (!moduleToPrefix.containsValue(entry.getKey())) {
                var qnameModule = entry.getValue();
                if (qnameModule != null) {
                    prefixesToQnames.put(entry.getKey(), qnameModule);
                }
            }
        }

        if (ctx.producesDeclared(SubmoduleStatement.class)) {
            final var belongsTo = resolvedInfo.belongsTo();
            if (belongsTo != null) {
                if (!prefixesToQnames.containsValue(belongsTo.parentModuleQname())) {
                    prefixesToQnames.put(belongsTo.prefix(), belongsTo.parentModuleQname());
                }
            }
        }

        prefixToModule = ImmutableMap.copyOf(prefixesToQnames);
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
