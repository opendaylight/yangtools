/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

public class QNameToStatementDefinitionMap implements QNameToStatementDefinition {
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
        Preconditions.checkNotNull(qname);
        Preconditions.checkNotNull(stDef);

        qnameToSupport.put(qname, stDef);
        putNoRev(qname, stDef);
    }

    public void putAll(final Map<QName, StatementSupport<?, ?, ?>> qnameToStmt) {
        qnameToSupport.putAll(qnameToStmt);
        qnameToStmt.forEach((this::putNoRev));
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
        final QName norev;
        if (qname.getRevision() != null) {
            norev = QName.create(qname.getNamespace(), null, qname.getLocalName()).intern();
        } else {
            norev = qname;
        }
        noRevQNameToSupport.put(norev, support);
    }

    @Nullable
    @Override
    public StatementSupport<?, ?, ?> get(@Nonnull final QName identifier) {
        return qnameToSupport.get(identifier);
    }

    @Nullable
    @Override
    public StatementDefinition getByNamespaceAndLocalName(@Nonnull final URI namespace, @Nonnull final String localName) {
        return noRevQNameToSupport.get(QName.create(namespace, null, localName));
    }

}
