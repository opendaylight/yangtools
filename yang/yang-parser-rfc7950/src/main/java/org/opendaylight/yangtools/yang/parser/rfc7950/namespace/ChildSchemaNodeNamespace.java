/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace.OnDemandStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Statement local namespace, which holds direct schema node descendants.
 */
@Beta
public final class ChildSchemaNodeNamespace<D extends DeclaredStatement<QName>,
            E extends SchemaTreeEffectiveStatement<D>>
        extends NamespaceBehaviour<QName, StmtContext<?, D, E>, ChildSchemaNodeNamespace<D, E>>
        implements StatementNamespace<QName, D, E> {
    public ChildSchemaNodeNamespace() {
        super((Class) ChildSchemaNodeNamespace.class);
    }

    @Override
    public StmtContext<?, D, E> get(final QName key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StmtContext<?, D, E> getFrom(final NamespaceStorageNode storage, final QName key) {
        // Get the backing storage node for the requested storage
        final NamespaceStorageNode storageNode = globalOrStatementSpecific(storage);
        // Check try to look up existing node
        final StmtContext<?, D, E> existing = storageNode.getFromLocalStorage(getIdentifier(), key);

        // An existing node takes precedence, if it does not exist try to request it
        return existing != null ? existing : requestFrom(storageNode, key);
    }

    private static <D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
            StmtContext<?, D, E> requestFrom(final NamespaceStorageNode storageNode, final QName key) {
        return storageNode instanceof OnDemandSchemaTreeStorageNode
            ? ((OnDemandSchemaTreeStorageNode) storageNode).requestSchemaTreeChild(key) : null;
    }

    @Override
    public Map<QName, StmtContext<?, D, E>> getAllFrom(final NamespaceStorageNode storage) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTo(final NamespaceStorageNode storage, final QName key, final StmtContext<?, D, E> value) {
        final StmtContext<?, D, E> prev = globalOrStatementSpecific(storage).putToLocalStorageIfAbsent(
            ChildSchemaNodeNamespace.class, key, value);

        if (prev != null) {
            throw new SourceException(value.getStatementSourceReference(),
                "Error in module '%s': cannot add '%s'. Node name collision: '%s' already declared at %s",
                value.getRoot().rawStatementArgument(), key, prev.getStatementArgument(),
                prev.getStatementSourceReference());
        }
    }

    /**
     * Find statement context identified by interpreting specified {@link SchemaNodeIdentifier} starting at specified
     * {@link StmtContext}.
     *
     * @param root Search root context
     * @param identifier {@link SchemaNodeIdentifier} relative to search root
     * @return Matching statement context, if present.
     * @throws NullPointerException if any of the arguments is null
     */
    public static Optional<StmtContext<?, ?, ?>> findNode(final StmtContext<?, ?, ?> root,
            final SchemaNodeIdentifier identifier) {
        final Iterator<QName> iterator = identifier.getNodeIdentifiers().iterator();
        if (!iterator.hasNext()) {
            return Optional.of(root);
        }

        QName nextPath = iterator.next();
        @SuppressWarnings("unchecked")
        StmtContext<?, ?, ?> current = (StmtContext<?, ?, ?>) root.getFromNamespace(ChildSchemaNodeNamespace.class,
            nextPath);
        if (current == null) {
            return Optional.ofNullable(tryToFindUnknownStatement(nextPath.getLocalName(), root));
        }
        while (current != null && iterator.hasNext()) {
            nextPath = iterator.next();
            @SuppressWarnings("unchecked")
            final StmtContext<?, ?, ?> nextNodeCtx = (StmtContext<?, ?, ?>) current.getFromNamespace(
                ChildSchemaNodeNamespace.class, nextPath);
            if (nextNodeCtx == null) {
                return Optional.ofNullable(tryToFindUnknownStatement(nextPath.getLocalName(), current));
            }
            current = nextNodeCtx;
        }
        return Optional.ofNullable(current);
    }

    @SuppressWarnings("unchecked")
    private static StmtContext<?, ?, ?> tryToFindUnknownStatement(final String localName,
            final StmtContext<?, ?, ?> current) {
        final Collection<? extends StmtContext<?, ?, ?>> unknownSubstatements = StmtContextUtils.findAllSubstatements(
            current, UnknownStatement.class);
        for (final StmtContext<?, ?, ?> unknownSubstatement : unknownSubstatements) {
            if (localName.equals(unknownSubstatement.rawStatementArgument())) {
                return unknownSubstatement;
            }
        }
        return null;
    }

    private static NamespaceStorageNode globalOrStatementSpecific(final NamespaceStorageNode storage) {
        NamespaceStorageNode current = storage;
        while (!isLocalOrGlobal(current.getStorageNodeType())) {
            current = current.getParentNamespaceStorage();
        }
        return current;
    }

    private static boolean isLocalOrGlobal(final StorageNodeType type) {
        return type == StorageNodeType.STATEMENT_LOCAL || type == StorageNodeType.GLOBAL;
    }
}
