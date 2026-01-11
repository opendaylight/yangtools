/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * An immutable {@link NamespaceBinding}.
 */
@NonNullByDefault
public record ImmutableNamespaceBinding(QName current, Map<Unqualified, QNameModule> prefixToModule)
        implements Immutable, NamespaceBinding {
    public ImmutableNamespaceBinding {
        requireNonNull(current);
        prefixToModule = Map.copyOf(prefixToModule);
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
        final var sb = new StringBuilder("ImmutableNamespaceBinding{currentModule=");
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
