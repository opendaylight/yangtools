/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.source.StatementDefinitionResolver;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

final class ReactorStatementDefinitionResolver implements Mutable, StatementDefinitionResolver {
    private record NSKey(String namespace, String localName) {
        NSKey {
            requireNonNull(namespace);
            requireNonNull(localName);
        }
    }

    private record QNKey(QNameModule namespace, String localName) {
        QNKey {
            requireNonNull(namespace);
            requireNonNull(localName);
        }
    }

    private final HashMap<QName, StatementSupport<?, ?, ?>> qnameToSupport = new HashMap<>();
    private final HashMap<NSKey, StatementDefinition<?, ?, ?>> norevToDef = new HashMap<>();
    private final HashMap<QNKey, StatementDefinition<?, ?, ?>> revToDef = new HashMap<>();

    @Override
    public StatementDefinition<?, ?, ?> lookupDef(final String namespace, final String localName) {
        return norevToDef.get(new NSKey(namespace, localName));
    }

    @Override
    public StatementDefinition<?, ?, ?> lookupDef(final QNameModule namespace, final String localName) {
        return revToDef.get(new QNKey(namespace, localName));
    }

    /**
     * {@return {@link StatementSupport} with specified {@code QName}}
     * @param identifier {@code QName} of requested statement
     */
    @Nullable StatementSupport<?, ?, ?> lookupSupport(final @NonNull QName identifier) {
        return qnameToSupport.get(requireNonNull(identifier));
    }

    @Nullable StatementSupport<?, ?, ?> tryAddSupport(final @NonNull QName identifier,
            final @NonNull StatementSupport<?, ?, ?> proposed) {
        final var existing = qnameToSupport.putIfAbsent(requireNonNull(identifier), requireNonNull(proposed));
        if (existing == null) {
            addDefinition(identifier, proposed.definition());
        }
        return existing;
    }

    void addSupports(final @NonNull Map<QName, StatementSupport<?, ?, ?>> additionalSupports) {
        for (var entry : additionalSupports.entrySet()) {
            addSupport(entry.getKey(), entry.getValue());
        }
    }

    @VisibleForTesting
    void addSupport(final QName qname, final StatementSupport<?, ?, ?> support) {
        qnameToSupport.put(requireNonNull(qname), requireNonNull(support));
        addDefinition(qname, support.definition());
    }

    private void addDefinition(final QName qname, final StatementDefinition<?, ?, ?> def) {
        norevToDef.put(new NSKey(qname.getNamespace().toString(), qname.getLocalName()), def);
        revToDef.put(new QNKey(qname.getModule(), qname.getLocalName()), def);
    }
}
