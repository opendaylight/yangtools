/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.Collection;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

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
     * Create a {@link DeclaredStatement} for specified context.
     *
     * @param ctx Statement context
     * @return A declared statement instance.
     */
    @NonNull D createDeclared(@NonNull StmtContext<A, D, ?> ctx);

    /**
     * Create a {@link EffectiveStatement} for specified context.
     *
     * @param stmt Effective capture of this statement's significant state
     * @return An effective statement instance
     */
    @NonNull E createEffective(EffectiveStmtCtx.@NonNull Current<A, D> stmt,
        Stream<? extends StmtContext<?, ?, ?>> declaredSubstatements,
        Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements);

    // FIXME: add javadocs
    boolean canReuseCurrent(@NonNull Current<A, D> copy, @NonNull Current<A, D> current,
        @NonNull Collection<? extends EffectiveStatement<?, ?>> substatements);

//    /**
//     * Determine reactor copy behaviour of a statement instance. Statement support classes are required to determine
//     * their operations with regard to their statements being replicated into different contexts, so that
//     * {@link Mutable} instances are not created when it is evident they are superfluous.
//     *
//     * <p>
//     * The copy operation has three intrinsic parts:
//     * <ul>
//     *   <li>target {@code parent}, i.e. new parent statement for the copy. This determines things like default value
//     *       of the {@code config} statement and similar</li>
//     *   <li>copy operation type</li>
//     *   <li>{@code target module}, which defines the default namespace for the statement copy. This might not be always
//     *       present, in which case the namespace is retained from the source. As an example, {@code uses} changes
//     *       the default namespace to parent's namespace, whereas {@code augment} does not.</li>
//     * </ul>
//     *
//     * <p>
//     * Implementations should return the context to use -- returning {@code stmt} if there is no change or a copy of it.
//     *
//     * @param stmt Context of statement to be copied statement
//     * @param parent Parent statement context
//     * @param copyType Type of copy being performed
//     * @param targetModule Target module, if present
//     * @return StmtContext holding the effective state
//     */
//    @NonNull Current<A, D> effectiveCopyOf(Current<A, D> stmt, Parent parent, CopyType copyType,
//        @Nullable QNameModule targetModule);
}
