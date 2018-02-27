/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

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
                ImmutableMap.toImmutableMap(SchemaTreeEffectiveStatement::getIdentifier, Functions.identity()));

            if (this instanceof DataTreeAwareEffectiveStatement && !schemaTreeNamespace.isEmpty()) {
                final Map<QName, DataTreeEffectiveStatement<?>> dataChildren = new LinkedHashMap<>();
                final boolean sameAsSchema = recursiveDataTreeChildren(ctx.getStatementSourceReference(), dataChildren,
                    schemaTreeNamespace.values());

                // This is a mighty hack to lower memory usage: if we consumed all schema tree children as data nodes,
                // the two maps are equal and hence we can share the instance.
                try {
                    dataTreeNamespace = sameAsSchema ? (Map) schemaTreeNamespace : ImmutableMap.copyOf(dataChildren);
                } catch (IllegalArgumentException e) {
                    throw new SourceException("Conflicting data children detected", ctx.getStatementSourceReference(),
                        e);
                }
            } else {
                dataTreeNamespace = ImmutableMap.of();
            }
        } else {
            dataTreeNamespace = ImmutableMap.of();
            schemaTreeNamespace = ImmutableMap.of();
        }
    }

    private static boolean recursiveDataTreeChildren(final StatementSourceReference ref,
            final Map<QName, DataTreeEffectiveStatement<?>> dataChildren,
            final Collection<? extends SchemaTreeEffectiveStatement<?>> statements) {
        boolean sameAsSchema = true;

        for (SchemaTreeEffectiveStatement<?> child : statements) {
            if (child instanceof DataTreeEffectiveStatement) {
                final QName id = child.getIdentifier();
                final DataTreeEffectiveStatement<?> prev = dataChildren.putIfAbsent(id,
                    (DataTreeEffectiveStatement<?>) child);
                SourceException.throwIf(prev != null, ref,
                        "Cannot add data child with name %s, a conflicting child already exists", id);
                continue;
            }

            // Deal with other SchemaTreeEffectiveStatements, but we are already tainted
            sameAsSchema = false;

            // For choice statements go through all their cases and fetch their data children
            if (child instanceof ChoiceEffectiveStatement) {
                child.streamEffectiveSubstatements(CaseEffectiveStatement.class)
                .map(stmt -> stmt.streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class))
                .forEach(strm -> recursiveDataTreeChildren(ref, dataChildren, strm.collect(Collectors.toList())));
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
