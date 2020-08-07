/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * Baseline stateless implementation of an EffectiveStatement. This class adds a few default implementations and
 * namespace dispatch, but does not actually force any state on its subclasses. This approach adds requirements for an
 * implementation, but it leaves it up to the final class to provide object layout.
 *
 * <p>
 * This finds immense value in catering the common case, for example effective statements which can, but typically
 * do not, contain substatements.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
@Beta
public abstract class AbstractEffectiveStatement<A, D extends DeclaredStatement<A>>
        extends AbstractModelStatement<A> implements EffectiveStatement<A, D> {
    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends V> get(final Class<N> namespace,
            final K identifier) {
        return Optional.ofNullable(getAll(namespace).get(requireNonNull(identifier)));
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        final Optional<? extends Map<K, V>> ret = getNamespaceContents(requireNonNull(namespace));
        return ret.isPresent() ? ret.get() : ImmutableMap.of();
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return ImmutableList.of();
    }

    /**
     * Return the statement-specific contents of specified namespace, if available.
     *
     * @param namespace Requested namespace
     * @return Namespace contents, if available.
     */
    protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final @NonNull Class<N> namespace) {
        return Optional.empty();
    }

    /**
     * Utility method for recovering singleton lists squashed by {@link #maskList(ImmutableList)}.
     *
     * @param masked list to unmask
     * @return Unmasked list
     * @throws NullPointerException if masked is null
     * @throws ClassCastException if masked object does not match EffectiveStatement
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> unmaskList(
            final @NonNull Object masked) {
        return (ImmutableList) unmaskList(masked, EffectiveStatement.class);
    }

    // TODO: below methods need to find a better place, this is just a temporary hideout as their public class is on
    //       its way out
    static @NonNull Map<QName, SchemaTreeEffectiveStatement<?>> createSchemaTreeNamespace(
            final StatementSourceReference ref, final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        final Map<QName, SchemaTreeEffectiveStatement<?>> schemaChildren = new LinkedHashMap<>();
        substatements.stream().filter(SchemaTreeEffectiveStatement.class::isInstance)
            .forEach(child -> putChild(schemaChildren, (SchemaTreeEffectiveStatement) child, ref, "schema"));
        return schemaChildren;
    }

    static @NonNull ImmutableMap<QName, DataTreeEffectiveStatement<?>> createDataTreeNamespace(
            final StatementSourceReference ref,
            final Collection<SchemaTreeEffectiveStatement<?>> schemaTreeStatements,
            // Note: this dance is needed to not retain ImmutableMap$Values
            final ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace) {
        final Map<QName, DataTreeEffectiveStatement<?>> dataChildren = new LinkedHashMap<>();
        boolean sameAsSchema = true;

        for (SchemaTreeEffectiveStatement<?> child : schemaTreeStatements) {
            if (child instanceof DataTreeEffectiveStatement) {
                putChild(dataChildren, (DataTreeEffectiveStatement<?>) child, ref, "data");
            } else {
                sameAsSchema = false;
                putChoiceDataChildren(dataChildren, ref, child);
            }
        }

        // This is a mighty hack to lower memory usage: if we consumed all schema tree children as data nodes,
        // the two maps are equal and hence we can share the instance.
        return sameAsSchema ? (ImmutableMap) schemaTreeNamespace : ImmutableMap.copyOf(dataChildren);
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
