/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

/**
 * An entity capable of creating {@link DeclaredStatement} and {@link EffectiveStatement} instances for a particular
 * type. This interface is usually realized as an implementation-specific combination with {@link StatementSupport}.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public interface StatementFactory<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {
    /**
     * Minimum amount of parent state required to build an accurate effective view of a particular child. Child state
     * is expressed as {@link EffectiveStatementState}.
     */
    interface EffectiveParentState extends Immutable {
        // FIXME: this needs to be a tri-state
        // FIXME: deprecate this method after we have gotten rid of StmtContext.getSchemaPath() users
        @NonNull Optional<SchemaPath> schemaPath();

        // FIXME: this should be Optional<Boolean>
        boolean effectiveConfig();

        // FIXME: add others
    }

    /**
     * Minimum amount of state required to build an accurate effective view of a statement. Parent state is expressed
     * as {@link EffectiveParentState}.
     */
    interface EffectiveStatementState<A, D extends DeclaredStatement<A>> extends Immutable {
        @NonNull StatementSource source();

        @NonNull CopyHistory history();

        @NonNull D declared();

        // Note: this collection is always recomputed
        @NonNull Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements();

        // Note: this collection is always recomputed
        @NonNull Collection<? extends StmtContext<?, ?, ?>> effectiveSubstatements();

        <K, V, T extends K, N extends IdentifierNamespace<K, V>> @Nullable V getFromNamespace(Class<@NonNull N> type,
            T key);

    }

    /**
     * Create a {@link DeclaredStatement} for specified context.
     *
     * @param ctx Statement context
     * @return A declared statement instance.
     */
    @NonNull D createDeclared(@NonNull StmtContext<A, D, ?> ctx);

    /**
     * Create a {@link EffectiveStatement} for specified context.
     *
     * @param ctx Statement context
     * @return An effective statement instance.
     */
    @NonNull E createEffective(@NonNull StmtContext<A, D, E> ctx);

    /**
     * Create a {@link EffectiveStatement} for specified context.
     *
     * @param parent Effective capture of parent's significant state
     * @param stmt Effective capture of this statement's significant state
     * @param ctx Statement context
     * @return An effective statement instance.
     */
    // FIXME: migrate users here if possible
    // FIXME: we want to avoid passing StmtContext in
    default @NonNull E createEffective(final @NonNull StmtContext<A, D, E> ctx,
            final @NonNull EffectiveParentState parent, final @NonNull EffectiveStatementState<A, D> stmt) {
        return createEffective(ctx);
    }
}
