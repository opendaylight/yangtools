/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public class QNameToStatementDefinitionMap implements QNameToStatementDefinition {

    private Map<QName, StatementDefinition> qNameToStmtDefMap = new HashMap<>();

    public void put(QName qName, StatementDefinition stDef) {
        qNameToStmtDefMap.put(qName, stDef);
    }

    @Nullable
    @Override
    public StatementDefinition get(@Nonnull QName identifier) {
        return qNameToStmtDefMap.get(identifier);
    }

    @Override
    public StatementDefinition getByNamespaceAndLocalName(URI namespace, String localName) {
        for (Entry<QName, StatementDefinition> entry : qNameToStmtDefMap.entrySet()) {
            QName qName = entry.getKey();
            if (qName.getNamespace().equals(namespace) && qName.getLocalName().equals(localName)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
