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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Statement local namespace, which holds direct schema node descendants.
 */
@Beta
public final class ChildSchemaNodeNamespace<D extends DeclaredStatement<QName>, E extends EffectiveStatement<QName, D>>
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
        return globalOrStatementSpecific(storage).getFromLocalStorage(getIdentifier(), key);
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
                value.getRoot().getStatementArgument(), key, prev.getStatementArgument(),
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
        final Iterator<QName> iterator = identifier.getPathFromRoot().iterator();
        if (!iterator.hasNext()) {
            return Optional.of(root);
        }

        QName nextPath = iterator.next();
        @SuppressWarnings("unchecked")
        Mutable<?, ?, EffectiveStatement<?, ?>> current = (Mutable)root.getFromNamespace(ChildSchemaNodeNamespace.class,
            nextPath);
        if (current == null) {
            return Optional.ofNullable(tryToFindUnknownStatement(nextPath.getLocalName(),
                (Mutable<?, ?, EffectiveStatement<?, ?>>) root));
        }
        while (current != null && iterator.hasNext()) {
            nextPath = iterator.next();
            @SuppressWarnings("unchecked")
            final Mutable<?, ?, EffectiveStatement<?, ?>> nextNodeCtx = (Mutable)current.getFromNamespace(
                ChildSchemaNodeNamespace.class, nextPath);
            if (nextNodeCtx == null) {
                return Optional.ofNullable(tryToFindUnknownStatement(nextPath.getLocalName(), current));
            }
            current = nextNodeCtx;
        }
        return Optional.ofNullable(current);
    }

    @SuppressWarnings("unchecked")
    static Mutable<?, ?, EffectiveStatement<?, ?>> tryToFindUnknownStatement(final String localName,
            final Mutable<?, ?, EffectiveStatement<?, ?>> current) {
        final Collection<? extends StmtContext<?, ?, ?>> unknownSubstatements = StmtContextUtils.findAllSubstatements(
            current, UnknownStatement.class);
        for (final StmtContext<?, ?, ?> unknownSubstatement : unknownSubstatements) {
            if (localName.equals(unknownSubstatement.rawStatementArgument())) {
                return (Mutable<?, ?, EffectiveStatement<?, ?>>) unknownSubstatement;
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
