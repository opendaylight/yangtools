/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import com.google.common.annotations.Beta;
import java.util.Iterator;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedNamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Legacy namespace for looking up nodes by their Schema Tree identifier.
 *
 * @deprecated Use path-based utilities provided around {@link ChildSchemaNodeNamespace} instead.
 */
@Beta
@Deprecated
public final class SchemaNodeIdentifierBuildNamespace
        extends DerivedNamespaceBehaviour<SchemaNodeIdentifier, Mutable<?, ?, EffectiveStatement<?, ?>>, QName,
                SchemaNodeIdentifierBuildNamespace, ChildSchemaNodeNamespace<?, ?>>
        implements IdentifierNamespace<SchemaNodeIdentifier, Mutable<?, ?, EffectiveStatement<?, ?>>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SchemaNodeIdentifierBuildNamespace() {
        super(SchemaNodeIdentifierBuildNamespace.class, (Class) ChildSchemaNodeNamespace.class);
    }

    /**
     * Find statement context identified by interpreting specified {@link SchemaNodeIdentifier} starting at specified
     * {@link StmtContext}.
     *
     * @param root Search root context
     * @param identifier {@link SchemaNodeIdentifier} relative to search root
     * @return Matching statement context, if present.
     * @throws NullPointerException if any of the arguments is null
     * @deprecated Use {@link ChildSchemaNodeNamespace#findNode(StmtContext, SchemaNodeIdentifier)} instead.
     */
    @Deprecated
    public static Optional<StmtContext<?, ?, ?>> findNode(final StmtContext<?, ?, ?> root,
            final SchemaNodeIdentifier identifier) {
        return ChildSchemaNodeNamespace.findNode(root, identifier);
    }

    @Override
    public Mutable<?, ?, EffectiveStatement<?, ?>> get(final SchemaNodeIdentifier key) {
        throw new UnsupportedOperationException("Direct access to namespace is not supported");
    }

    @Override
    public Mutable<?, ?, EffectiveStatement<?, ?>> getFrom(final NamespaceStorageNode storage,
            final SchemaNodeIdentifier key) {
        final NamespaceStorageNode lookupStartStorage;
        if (key.isAbsolute() || storage.getStorageNodeType() == StorageNodeType.ROOT_STATEMENT_LOCAL) {
            lookupStartStorage = NamespaceBehaviour.findClosestTowardsRoot(storage, StorageNodeType.GLOBAL);
        } else {
            lookupStartStorage = storage;
        }
        final Iterator<QName> iterator = key.getPathFromRoot().iterator();
        if (!iterator.hasNext()) {
            if (lookupStartStorage instanceof StmtContext<?, ?, ?>) {
                return (Mutable<?, ?, EffectiveStatement<?, ?>>) lookupStartStorage;
            }
            return null;
        }
        QName nextPath = iterator.next();
        @SuppressWarnings("unchecked")
        Mutable<?, ?, EffectiveStatement<?, ?>> current = (Mutable)lookupStartStorage.getFromLocalStorage(
            ChildSchemaNodeNamespace.class,nextPath);
        if (current == null && lookupStartStorage instanceof StmtContext<?, ?, ?>) {
            return ChildSchemaNodeNamespace.tryToFindUnknownStatement(nextPath.getLocalName(),
                (Mutable<?, ?, EffectiveStatement<?, ?>>) lookupStartStorage);
        }
        while (current != null && iterator.hasNext()) {
            nextPath = iterator.next();
            @SuppressWarnings("unchecked")
            final Mutable<?, ?, EffectiveStatement<?, ?>> nextNodeCtx = (Mutable)current.getFromNamespace(
                ChildSchemaNodeNamespace.class,nextPath);
            if (nextNodeCtx == null) {
                return ChildSchemaNodeNamespace.tryToFindUnknownStatement(nextPath.getLocalName(), current);
            }
            current = nextNodeCtx;
        }
        return current;
    }

    @Override
    public QName getSignificantKey(final SchemaNodeIdentifier key) {
        return key.getLastComponent();
    }
}
