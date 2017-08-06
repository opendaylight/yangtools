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
import javax.annotation.Nonnull;
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

public class SchemaNodeIdentifierBuildNamespace extends
        DerivedNamespaceBehaviour<SchemaNodeIdentifier, StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>, QName, SchemaNodeIdentifierBuildNamespace, ChildSchemaNodes<?, ?>>
        implements IdentifierNamespace<SchemaNodeIdentifier, StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SchemaNodeIdentifierBuildNamespace() {
        super(SchemaNodeIdentifierBuildNamespace.class, (Class) ChildSchemaNodes.class);
    }

    @Override
    public StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>> get(
            @Nonnull final SchemaNodeIdentifier key) {
        throw new UnsupportedOperationException("Direct access to namespace is not supported");
    }

    @SuppressWarnings("unchecked")
    @Override
    public StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>> getFrom(final NamespaceStorageNode storage, final SchemaNodeIdentifier key) {

        final NamespaceStorageNode lookupStartStorage;
        if (key.isAbsolute() || storage.getStorageNodeType() == StorageNodeType.ROOT_STATEMENT_LOCAL) {
            lookupStartStorage = NamespaceBehaviour.findClosestTowardsRoot(storage, StorageNodeType.GLOBAL);
        } else {
            lookupStartStorage = storage;
        }
        final Iterator<QName> iterator = key.getPathFromRoot().iterator();
        if (!iterator.hasNext()) {
            if (lookupStartStorage instanceof StmtContext<?, ?, ?>) {
                return (StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>) lookupStartStorage;
            }
            return null;
        }
        QName nextPath = iterator.next();
        StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>> current = lookupStartStorage
                .getFromLocalStorage(ChildSchemaNodes.class, nextPath);
        if (current == null && lookupStartStorage instanceof StmtContext<?, ?, ?>) {
            return tryToFindUnknownStatement(nextPath.getLocalName(), (Mutable<?, ?, EffectiveStatement<?, ?>>) lookupStartStorage);
        }
        while (current != null && iterator.hasNext()) {
            nextPath = iterator.next();
            final StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>> nextNodeCtx = current
                    .getFromNamespace(ChildSchemaNodes.class, nextPath);
            if (nextNodeCtx == null) {
                return tryToFindUnknownStatement(nextPath.getLocalName(), current);
            }
            current = nextNodeCtx;
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private static Mutable<?, ?, EffectiveStatement<?, ?>> tryToFindUnknownStatement(final String localName,
            final Mutable<?, ?, EffectiveStatement<?, ?>> current) {
        final Collection<StmtContext<?, ?, ?>> unknownSubstatements = StmtContextUtils.findAllSubstatements(current,
                UnknownStatement.class);
        for (final StmtContext<?, ?, ?> unknownSubstatement : unknownSubstatements) {
            if (localName.equals(unknownSubstatement.rawStatementArgument())) {
                return (Mutable<?, ?, EffectiveStatement<?, ?>>) unknownSubstatement;
            }
        }
        return null;
    }

    @Override
    public QName getSignificantKey(final SchemaNodeIdentifier key) {
        return key.getLastComponent();
    }

}
