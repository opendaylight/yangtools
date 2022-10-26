/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.AbstractEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespacedEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * Baseline stateless implementation of an EffectiveStatement. This class adds a few default implementations and
 * namespace dispatch, but does not actually force any state on its subclasses. This approach adds requirements for an
 * implementation, but it leaves it up to the final class to provide object layout.
 *
 * <p>
 * This finds immense value in catering the common case, for example effective statements which can, but typically
 * do not, contain substatements.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
abstract sealed class AbstractIndexedEffectiveStatement<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveStatement<A, D>
        permits AbstractDeclaredEffectiveStatement, AbstractUndeclaredEffectiveStatement {
    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return ImmutableList.of();
    }

    // TODO: below methods need to find a better place, this is just a temporary hideout as their public class is on
    //       its way out
    /**
     * Create a Map containing the contents of the schema tree. Retur
     * @param substatements Substatements to index
     * @return Index of the schema tree as a mutable Map
     * @throws NullPointerException if {@code substatements} is null
     */
    protected static @NonNull Map<QName, SchemaTreeEffectiveStatement<?>> createSchemaTreeNamespace(
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        final Map<QName, SchemaTreeEffectiveStatement<?>> schemaChildren = new LinkedHashMap<>();
        substatements.stream().filter(SchemaTreeEffectiveStatement.class::isInstance)
            .forEach(child -> putChild(schemaChildren, (SchemaTreeEffectiveStatement<?>) child, "schema tree"));
        return schemaChildren;
    }

    protected static @NonNull Map<QName, DataTreeEffectiveStatement<?>> createDataTreeNamespace(
            final Collection<SchemaTreeEffectiveStatement<?>> schemaTreeStatements,
            // Note: this dance is needed to not retain ImmutableMap$Values
            final Map<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace) {
        final Map<QName, DataTreeEffectiveStatement<?>> dataChildren = new LinkedHashMap<>();
        boolean sameAsSchema = true;

        for (SchemaTreeEffectiveStatement<?> child : schemaTreeStatements) {
            if (!indexDataTree(dataChildren, child)) {
                sameAsSchema = false;
            }
        }

        // This is a mighty hack to lower memory usage: if we consumed all schema tree children as data nodes,
        // the two maps are equal and hence we can share the instance.
        return sameAsSchema ? (Map) schemaTreeNamespace : immutableNamespaceOf(dataChildren);
    }

    protected static <T extends SchemaTreeEffectiveStatement<?>> @NonNull Map<QName, T> immutableNamespaceOf(
            final Map<QName, T> map) {
        return map.size() == 1 ? new SingletonNamespace<>(map.values().iterator().next()) : ImmutableMap.copyOf(map);
    }

    protected static @NonNull HashMap<QName, TypedefEffectiveStatement> createTypedefNamespace(
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        final HashMap<QName, TypedefEffectiveStatement> typedefs = new HashMap<>();

        substatements.stream().filter(TypedefEffectiveStatement.class::isInstance)
            .forEach(child -> putChild(typedefs, (TypedefEffectiveStatement) child, "typedef"));

        return typedefs;
    }

    private static boolean indexDataTree(final Map<QName, DataTreeEffectiveStatement<?>> map,
            final EffectiveStatement<?, ?> stmt) {
        if (stmt instanceof DataTreeEffectiveStatement) {
            putChild(map, (DataTreeEffectiveStatement<?>) stmt, "data tree");
            return true;
        } else if (stmt instanceof ChoiceEffectiveStatement) {
            // For choice statements go through all their cases and fetch their data children
            for (EffectiveStatement<?, ?> choiceChild : stmt.effectiveSubstatements()) {
                if (choiceChild instanceof CaseEffectiveStatement) {
                    for (EffectiveStatement<?, ?> caseChild : choiceChild.effectiveSubstatements()) {
                        indexDataTree(map, caseChild);
                    }
                }
            }
        } else if (stmt instanceof CaseEffectiveStatement) {
            // For case statements go through all their statements
            for (EffectiveStatement<?, ?> child : stmt.effectiveSubstatements()) {
                indexDataTree(map, child);
            }
        }
        return false;
    }

    private static <T extends NamespacedEffectiveStatement<?>> void putChild(final Map<QName, T> map, final T child,
            final String namespace) {
        final QName id = child.getIdentifier();
        final T prev = map.putIfAbsent(id, child);
        if (prev != null) {
            throw new SubstatementIndexingException(
                "Cannot add " + namespace + " child with name " + id + ", a conflicting child already exists");
        }
    }
}
