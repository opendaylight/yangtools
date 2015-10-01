/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Statement local namespace, which holds direct schema node descendants.
 *
 * @param <D>
 * @param <E>
 */
public class ChildSchemaNodes<D extends DeclaredStatement<QName>,E extends EffectiveStatement<QName, D>>
    extends NamespaceBehaviour<QName, StmtContext<?, D, E>, ChildSchemaNodes<D, E>>
    implements StatementNamespace<QName, D, E>{

    protected ChildSchemaNodes() {
        super((Class<ChildSchemaNodes<D,E>>) (Class) ChildSchemaNodes.class);
    }

    @Override
    public StmtContext<?, D, E> get(QName key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StmtContext<?, D, E> getFrom(NamespaceStorageNode storage, QName key) {
        return globalOrStatementSpecific(storage).getFromLocalStorage(getIdentifier(), key);
    }

    @Override
    public Map<QName, StmtContext<?, D, E>> getAllFrom(NamespaceStorageNode storage) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTo(NamespaceBehaviour.NamespaceStorageNode storage, QName key, StmtContext<?, D, E> value) {
        globalOrStatementSpecific(storage).addToLocalStorage(ChildSchemaNodes.class, key, value);
        ((StmtContext.Mutable<?,?,?>) value).addToNs((Class) SchemaNodeIdentifierBuildNamespace.class, null, value);
    }

    private NamespaceStorageNode globalOrStatementSpecific(NamespaceBehaviour.NamespaceStorageNode storage) {
        NamespaceStorageNode current = storage;
        while(current.getStorageNodeType() != StorageNodeType.STATEMENT_LOCAL && current.getStorageNodeType() != StorageNodeType.GLOBAL) {
            current = current.getParentNamespaceStorage();
        }
        return current;
    }
}
