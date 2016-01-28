/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedNamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

class SchemaNodeIdentifierBuildNamespace extends
        DerivedNamespaceBehaviour<SchemaNodeIdentifier, StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>, QName, SchemaNodeIdentifierBuildNamespace, ChildSchemaNodes<?, ?>>
        implements IdentifierNamespace<SchemaNodeIdentifier, StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected SchemaNodeIdentifierBuildNamespace() {
        super(SchemaNodeIdentifierBuildNamespace.class, (Class) ChildSchemaNodes.class);
    }

    @Override
    public StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>> get(
            SchemaNodeIdentifier key) {
        throw new UnsupportedOperationException("Direct access to namespace is not supported");
    }

    @SuppressWarnings("unchecked")
    @Override
    public StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>> getFrom(NamespaceStorageNode storage, SchemaNodeIdentifier key) {

        final NamespaceStorageNode lookupStartStorage;
        if(key.isAbsolute() || storage.getStorageNodeType() == StorageNodeType.ROOT_STATEMENT_LOCAL) {
            lookupStartStorage = NamespaceBehaviour.findClosestTowardsRoot(storage, StorageNodeType.GLOBAL);
        } else {
            lookupStartStorage = storage;
        }
        Iterator<QName> iterator = key.getPathFromRoot().iterator();
        if(!iterator.hasNext()) {
            if(lookupStartStorage instanceof StmtContext<?, ?, ?>) {
                return (StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>) lookupStartStorage;
            } else {
                return null;
            }
        }
        QName nextPath = iterator.next();
        StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>> current = (StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>) lookupStartStorage
                .getFromLocalStorage(ChildSchemaNodes.class, nextPath);
        if(current == null && lookupStartStorage instanceof StmtContext<?, ?, ?>) {
            return tryToFindUnknownStatement(nextPath.getLocalName(), (Mutable<?, ?, EffectiveStatement<?, ?>>) lookupStartStorage);
        }
        while (current != null && iterator.hasNext()) {
            nextPath = iterator.next();
            StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>> nextNodeCtx = (StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>) current
                    .getFromNamespace(ChildSchemaNodes.class, nextPath);
            if (nextNodeCtx == null) {
                return tryToFindUnknownStatement(nextPath.getLocalName(), current);
            } else {
                current = nextNodeCtx;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private Mutable<?, ?, EffectiveStatement<?, ?>> tryToFindUnknownStatement(final String localName,
            final Mutable<?, ?, EffectiveStatement<?, ?>> current) {
        Collection<StmtContext<?, ?, ?>> unknownSubstatements = StmtContextUtils.findAllSubstatements(current,
                UnknownStatement.class);
        for (StmtContext<?, ?, ?> unknownSubstatement : unknownSubstatements) {
            if(unknownSubstatement.rawStatementArgument().equals(localName)) {
                return (Mutable<?, ?, EffectiveStatement<?, ?>>) unknownSubstatement;
            }
        }
        return null;
    }

    @Override
    public QName getSignificantKey(SchemaNodeIdentifier key) {
        return key.getLastComponent();
    }

}
