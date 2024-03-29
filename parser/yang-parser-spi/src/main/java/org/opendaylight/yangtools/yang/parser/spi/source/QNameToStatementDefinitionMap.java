/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

public final class QNameToStatementDefinitionMap implements QNameToStatementDefinition {
    private final Map<QName, StatementSupport<?, ?, ?>> noRevQNameToSupport;
    private final Map<QName, StatementSupport<?, ?, ?>> qnameToSupport;

    public QNameToStatementDefinitionMap() {
        noRevQNameToSupport = new HashMap<>();
        qnameToSupport = new HashMap<>();
    }

    public QNameToStatementDefinitionMap(final int initialCapacity) {
        noRevQNameToSupport = new HashMap<>(initialCapacity);
        qnameToSupport = new HashMap<>(initialCapacity);
    }

    public void put(final QName qname, final StatementSupport<?, ?, ?> stDef) {
        // HashMap does not guard against nulls
        qnameToSupport.put(requireNonNull(qname), requireNonNull(stDef));
        putNoRev(qname, stDef);
    }

    public void putAll(final Map<QName, StatementSupport<?, ?, ?>> qnameToStmt) {
        qnameToSupport.putAll(qnameToStmt);
        qnameToStmt.forEach(this::putNoRev);
    }

    public StatementSupport<?, ?, ?> putIfAbsent(final QName qname, final StatementSupport<?, ?, ?> support) {
        final StatementSupport<?, ?, ?> existing = qnameToSupport.putIfAbsent(qname, support);
        if (existing != null) {
            return existing;
        }

        // XXX: we can (in theory) conflict here if we ever find ourselves needing to have multiple revisions of
        //      statements. These should be equivalent, so no harm done (?)
        //      Anyway, this is how it worked before last refactor.
        putNoRev(qname, support);
        return null;
    }

    private void putNoRev(final QName qname, final StatementSupport<?, ?, ?> support) {
        final QName norev = qname.withoutRevision();
        noRevQNameToSupport.put(norev != qname ? norev.intern() : qname, support);
    }

    @Override
    public StatementDefinition get(final QName identifier) {
        return definitionOf(getSupport(identifier));
    }

    @Override
    public StatementDefinition getByNamespaceAndLocalName(final XMLNamespace namespace, final String localName) {
        return definitionOf(noRevQNameToSupport.get(QName.create(namespace, localName)));
    }

    /**
     * Returns StatementSupport with specified QName.
     *
     * @param identifier QName of requested statement
     * @return StatementSupport
     */
    public @Nullable StatementSupport<?, ?, ?> getSupport(final QName identifier) {
        return qnameToSupport.get(requireNonNull(identifier));
    }

    private static @Nullable StatementDefinition definitionOf(final @Nullable StatementSupport<?, ?, ?> support) {
        return support != null ? support.definition() : null;
    }
}
