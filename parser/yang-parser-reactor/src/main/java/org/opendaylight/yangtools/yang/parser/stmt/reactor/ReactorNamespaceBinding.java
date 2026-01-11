/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.NamespaceBinding;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;

/**
 * A simple {@link NamespaceBinding} implementation.
 */
@NonNullByDefault
record ReactorNamespaceBinding(QName current, Map<Unqualified, QNameModule> prefixToModule)
        implements NamespaceBinding {
    ReactorNamespaceBinding {
        requireNonNull(current);
        requireNonNull(prefixToModule);
    }

    static ReactorNamespaceBinding of(final RootStatementContext<?, ?, ?> root) {
        return new ReactorNamespaceBinding(root.moduleName(), indexNamespaces(root));
    }

    private static Map<Unqualified, QNameModule> indexNamespaces(final RootStatementContext<?, ?, ?> ctx) {
        final var prefixToModule = new HashMap<Unqualified, QNameModule>();

        // process all import statements
        final var importedModules = ctx.namespace(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX);
        if (importedModules != null) {
            for (var entry : importedModules.entrySet()) {
                final var module = ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, entry.getValue());
                if (module == null) {
                    throw new InferenceException(ctx, "Missing IMPORT_PREFIX_TO_MODULECTX linkage of " + entry);
                }

                prefixToModule.put(Unqualified.of(entry.getKey()), module);
            }
        }

        // submodules also define a prefix via 'belongs-to', which must not conflict with import prefixes
        if (ctx.producesDeclared(SubmoduleStatement.class)) {
            final var belongsToName = ctx.namespace(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME);
            if (belongsToName == null) {
                throw new InferenceException(ctx, "Missing BELONGSTO_PREFIX_TO_MODULE_NAME linkage");
            }
            if (belongsToName.size() != 1) {
                throw new InferenceException(ctx,
                    "Unexpected BELONGSTO_PREFIX_TO_MODULE_NAME linkage " + belongsToName);
            }

            final var entry = belongsToName.entrySet().iterator().next();
            final var belongsTo = ctx.namespaceItem(ParserNamespaces.MODULE_NAME_TO_QNAME, entry.getValue());
            if (belongsTo == null) {
                throw new InferenceException(ctx, "Missing MODULE_NAME_TO_QNAME linkage of " + entry);
            }

            final var prev = prefixToModule.putIfAbsent(Unqualified.of(entry.getKey()), belongsTo);
            if (prev != null) {
                final var sb = new StringBuilder("belongs-to prefix ").append(entry.getValue().getLocalName())
                    .append(" overlaps with import of ");
                appendModule(sb, prev);
                throw new InferenceException(ctx, sb.toString());
            }
        }

        return Map.copyOf(prefixToModule);
    }

    @Override
    public QNameModule currentModule() {
        return current.getModule();
    }

    @Override
    public @Nullable QNameModule lookupModule(final Unqualified prefix) {
        return prefixToModule.get(requireNonNull(prefix));
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder("ReactorNamespaceBinding{currentModule=");
        appendModule(sb, currentModule());
        sb.append(", prefixToModule={");

        // yeah, not entirely efficient
        final var it = prefixToModule.entrySet().stream()
            .sorted(Comparator.comparing(Entry::getKey))
            .toList()
            .iterator();
        if (it.hasNext()) {
            appendMapping(sb, it.next());
            while (it.hasNext()) {
                appendMapping(sb.append(", "), it.next());
            }
        }
        return sb.append("}}").toString();
    }

    private static void appendMapping(final StringBuilder sb, final Entry<Unqualified, QNameModule> mapping) {
        appendModule(sb.append(mapping.getKey().getLocalName()).append("="), mapping.getValue());
    }

    private static void appendModule(final StringBuilder sb, final QNameModule module) {
        sb.append(module.namespace());
        final var revision = module.revision();
        if (revision != null) {
            sb.append('@').append(revision);
        }
    }
}
