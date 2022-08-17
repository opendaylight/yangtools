/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Statement local namespace, which holds direct schema node descendants. This corresponds to the contents of the schema
 * tree as exposed through {@link SchemaTreeAwareEffectiveStatement}.
 */
// FIXME: 7.0.0: this contract seems to fall on the reactor side of things rather than parser-spi. Consider moving this
//               into yang-(parser-)reactor-api.
@Beta
public final class SchemaTreeNamespace<D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
        extends StatementNamespace<QName, D, E> {
    public static final class Behaviour<D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
            extends NamespaceBehaviour<QName, StmtContext<?, D, E>, SchemaTreeNamespace<D, E>> {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Behaviour() {
            super((Class) SchemaTreeNamespace.class);
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * This method is analogous to {@link SchemaTreeAwareEffectiveStatement#findSchemaTreeNode(QName)}.
         */
        @Override
        public StmtContext<?, D, E> getFrom(final NamespaceStorageNode storage, final QName key) {
            // Get the backing storage node for the requested storage
            final NamespaceStorageNode storageNode = globalOrStatementSpecific(storage);
            // Check try to look up existing node
            final StmtContext<?, D, E> existing = storageNode.getFromLocalStorage(getIdentifier(), key);

            // An existing node takes precedence, if it does not exist try to request it
            return existing != null ? existing : requestFrom(storageNode, key);
        }

        @Override
        public Map<QName, StmtContext<?, D, E>> getAllFrom(final NamespaceStorageNode storage) {
            // FIXME: 7.0.0: this method needs to be well-defined
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void addTo(final NamespaceStorageNode storage, final QName key, final StmtContext<?, D, E> value) {
            final StmtContext<?, D, E> prev = globalOrStatementSpecific(storage).putToLocalStorageIfAbsent(
                SchemaTreeNamespace.class, key, value);

            if (prev != null) {
                throw new SourceException(value,
                    "Error in module '%s': cannot add '%s'. Node name collision: '%s' already declared at %s",
                    value.getRoot().rawArgument(), key, prev.argument(), prev.sourceReference());
            }
        }

        private static <D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
                StmtContext<?, D, E> requestFrom(final NamespaceStorageNode storageNode, final QName key) {
            return storageNode instanceof OnDemandSchemaTreeStorageNode ondemand ? ondemand.requestSchemaTreeChild(key)
                : null;
        }

        private static NamespaceStorageNode globalOrStatementSpecific(final NamespaceStorageNode storage) {
            NamespaceStorageNode current = requireNonNull(storage);
            while (!isLocalOrGlobal(current.getStorageNodeType())) {
                current = verifyNotNull(current.getParentNamespaceStorage());
            }
            return current;
        }

        private static boolean isLocalOrGlobal(final StorageNodeType type) {
            return type == StorageNodeType.STATEMENT_LOCAL || type == StorageNodeType.GLOBAL;
        }
    }

    public static final @NonNull NamespaceBehaviour<?, ?, ?> BEHAVIOUR = new Behaviour<>();

    private SchemaTreeNamespace() {
        // Hidden on purpose
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
        StmtContext<?, ?, ?> current = (StmtContext<?, ?, ?>) root.getFromNamespace(SchemaTreeNamespace.class,
            nextPath);
        if (current == null) {
            return Optional.ofNullable(tryToFindUnknownStatement(nextPath.getLocalName(), root));
        }
        while (current != null && iterator.hasNext()) {
            nextPath = iterator.next();
            @SuppressWarnings("unchecked")
            final StmtContext<?, ?, ?> nextNodeCtx = (StmtContext<?, ?, ?>) current.getFromNamespace(
                SchemaTreeNamespace.class, nextPath);
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
            if (localName.equals(unknownSubstatement.rawArgument())) {
                return unknownSubstatement;
            }
        }
        return null;
    }
}
