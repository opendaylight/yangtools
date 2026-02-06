/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;

/**
 * A {@link NamespaceContext} implementation based on the set of imports and local module namespace.
 */
// TODO: this is a useful utility, so it may be useful to expose it either in this package, or yang.parser.spi.source
public final class ImmutableYangNamespaceContext implements YangNamespaceContext {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final Map<QNameModule, String> moduleToPrefix;
    private final Map<String, QNameModule> prefixToModule;

    @NonNullByDefault
    private ImmutableYangNamespaceContext(final Map<QNameModule, String> moduleToPrefix,
            final Map<String, QNameModule> prefixToModule) {
        this.moduleToPrefix = Map.copyOf(moduleToPrefix);
        this.prefixToModule = Map.copyOf(prefixToModule);
    }

    @NonNullByDefault
    public static ImmutableYangNamespaceContext of(final Map<Unqualified, QNameModule> prefixToModule) {
        final var toModule = HashMap.<String, QNameModule>newHashMap(prefixToModule.size());
        final var toPrefix = HashMap.<QNameModule, String>newHashMap(prefixToModule.size());
        for (var entry : prefixToModule.entrySet()) {
            final var prefix = entry.getKey().getLocalName();
            final var module = entry.getValue();

            // simple
            toModule.put(prefix, module);

            // slightly more complicated: use the lexicographically lowest prefix
            final var prev = toPrefix.putIfAbsent(module, prefix);
            if (prev != null && prev.compareTo(prefix) > 0) {
                toPrefix.replace(module, prev, prefix);
            }
        }

        return new ImmutableYangNamespaceContext(toPrefix, toModule);
    }

    @Override
    public String prefixForNamespace(final QNameModule namespace) {
        return moduleToPrefix.get(requireNonNull(namespace));
    }

    @Override
    public QNameModule namespaceForPrefix(final String prefix) {
        return prefixToModule.get(requireNonNull(prefix));
    }
}
