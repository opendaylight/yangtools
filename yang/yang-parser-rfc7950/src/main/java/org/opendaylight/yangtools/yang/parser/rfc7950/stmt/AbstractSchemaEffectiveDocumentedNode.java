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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
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
    private final ImmutableMap<QName, DataTreeEffectiveStatement<?>> dataTreeNamespace;
    private final ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace;

    protected AbstractSchemaEffectiveDocumentedNode(final StmtContext<A, D, ?> ctx) {
        super(ctx);

        if (this instanceof SchemaTreeAwareEffectiveStatement) {
            final StatementSourceReference ref = ctx.getStatementSourceReference();
            final Map<QName, SchemaTreeEffectiveStatement<?>> schemaChildren = new LinkedHashMap<>();
            streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class).forEach(child -> {
                putChild(schemaChildren, child, ref, "schema");
            });
            schemaTreeNamespace = ImmutableMap.copyOf(schemaChildren);

            if (this instanceof DataTreeAwareEffectiveStatement && !schemaTreeNamespace.isEmpty()) {
                final Map<QName, DataTreeEffectiveStatement<?>> dataChildren = new LinkedHashMap<>();
                boolean sameAsSchema = true;

                for (SchemaTreeEffectiveStatement<?> child : schemaTreeNamespace.values()) {
                    if (child instanceof DataTreeEffectiveStatement) {
                        putChild(dataChildren, (DataTreeEffectiveStatement<?>) child, ref, "data");
                    } else {
                        sameAsSchema = false;
                        putChoiceDataChildren(dataChildren, ref, child);
                    }
                }

                // This is a mighty hack to lower memory usage: if we consumed all schema tree children as data nodes,
                // the two maps are equal and hence we can share the instance.
                dataTreeNamespace = sameAsSchema ? (ImmutableMap) schemaTreeNamespace
                        : ImmutableMap.copyOf(dataChildren);
            } else {
                dataTreeNamespace = ImmutableMap.of();
            }
        } else {
            dataTreeNamespace = ImmutableMap.of();
            schemaTreeNamespace = ImmutableMap.of();
        }
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

    private static <T extends SchemaTreeEffectiveStatement<?>> void putChild(final Map<QName, T> map,
            final T child, final StatementSourceReference ref, final String tree) {
        final QName id = child.getIdentifier();
        final T prev = map.putIfAbsent(id, child);
        SourceException.throwIf(prev != null, ref,
                "Cannot add %s tree child with name %s, a conflicting child already exists", tree, id);
    }

    private static void putChoiceDataChildren(final Map<QName, DataTreeEffectiveStatement<?>> map,
            final StatementSourceReference ref, final SchemaTreeEffectiveStatement<?> child) {
        // For choice statements go through all their cases and fetch their data children
        if (child instanceof ChoiceEffectiveStatement) {
            child.streamEffectiveSubstatements(CaseEffectiveStatement.class).forEach(
                caseStmt -> caseStmt.streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class).forEach(stmt -> {
                    if (stmt instanceof DataTreeEffectiveStatement) {
                        putChild(map, (DataTreeEffectiveStatement<?>) stmt, ref, "data");
                    } else {
                        putChoiceDataChildren(map, ref, stmt);
                    }
                }));
        }
    }
}
