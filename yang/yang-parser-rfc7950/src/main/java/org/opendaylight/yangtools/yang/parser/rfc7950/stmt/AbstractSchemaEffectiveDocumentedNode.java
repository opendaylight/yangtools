/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * An {@link AbstractEffectiveDocumentedNode} which can optionally support {@link SchemaTreeAwareEffectiveStatement}.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 * @author Robert Varga
 */
@Beta
public abstract class AbstractSchemaEffectiveDocumentedNode<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveDocumentedNode<A, D> {

    private final Map<QName, DataTreeEffectiveStatement<?>> dataTreeNamespace;
    private final Map<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace;

    protected AbstractSchemaEffectiveDocumentedNode(final StmtContext<A, D, ?> ctx) {
        super(ctx);

        if (this instanceof SchemaTreeAwareEffectiveStatement) {
            schemaTreeNamespace = streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class).collect(
                ImmutableMap.toImmutableMap(SchemaTreeEffectiveStatement::getIdentifier, stmt -> stmt));
            if (this instanceof DataTreeAwareEffectiveStatement) {
                final Builder<QName, DataTreeEffectiveStatement<?>> b = ImmutableMap.builder();
                final boolean sameAsSchema = recursiveDataTreeChildren(b, schemaTreeNamespace.values());

                // This is a mighty hack to lower memory usage: if we consumed all schema tree children as data nodes,
                // the two maps are equal and hence we can share the instance.
                dataTreeNamespace = sameAsSchema ? (Map) schemaTreeNamespace : b.build();
            } else {
                dataTreeNamespace = ImmutableMap.of();
            }
        } else {
            dataTreeNamespace = ImmutableMap.of();
            schemaTreeNamespace = ImmutableMap.of();
        }
    }

    private static boolean recursiveDataTreeChildren(final Builder<QName, DataTreeEffectiveStatement<?>> builder,
            final Collection<? extends SchemaTreeEffectiveStatement<?>> statements) {
        boolean sameAsSchema = true;

        for (SchemaTreeEffectiveStatement<?> child : statements) {
            if (child instanceof DataTreeEffectiveStatement) {
                builder.put(child.getIdentifier(), (DataTreeEffectiveStatement<?>) child);
                continue;
            }

            // Deal with other SchemaTreeEffectiveStatements, but we are already tainted
            sameAsSchema = false;

            // For choice statements go through all their cases and fetch their data children
            if (child instanceof ChoiceEffectiveStatement) {
                recursiveDataTreeChildren(builder, child.findAll(CaseEffectiveStatementNamespace.class).values());
            }
        }

        return sameAsSchema;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final Class<N> namespace) {
        if (this instanceof SchemaTreeAwareEffectiveStatement
                && SchemaTreeAwareEffectiveStatement.Namespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) schemaTreeNamespace);
        }
        if (this instanceof DataTreeAwareEffectiveStatement
                && DataTreeAwareEffectiveStatement.Namespace.class.equals(namespace)) {
            return Optional.of((Map<K, V>) dataTreeNamespace);
        }
        return super.getNamespaceContents(namespace);
    }
}
