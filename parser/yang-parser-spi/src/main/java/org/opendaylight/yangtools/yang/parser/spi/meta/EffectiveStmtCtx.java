/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective view of a {@link StmtContext} for the purposes of creating an {@link EffectiveStatement}.
 */
@Beta
public non-sealed interface EffectiveStmtCtx extends CommonStmtCtx, StmtContextCompat, Immutable {
    @Override
    <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> EffectiveStmtCtx asDeclaring(
        StatementDefinition<X, Y, Z> def);

    @Override
    <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> EffectiveStmtCtx verifyDeclaring(
        StatementDefinition<X, Y, Z> def);

    /**
     * Return parent of this context, if there is one. All statements except for top-level source statements, such as
     * {@code module} and {@code submodule}.
     *
     * @return Parent context, or null if this statement is the root
     */
    @Nullable Parent effectiveParent();

    /**
     * Return parent of this context.
     *
     * @return Parent context
     * @throws VerifyException if this context is already the root
     */
    default @NonNull Parent getEffectiveParent() {
        final var ret = effectiveParent();
        if (ret == null) {
            throw new VerifyException("Attempted to access beyond root context");
        }
        return ret;
    }

    /**
     * Minimum amount of parent state required to build an accurate effective view of a particular child. Child state
     * is expressed as {@link Current}.
     */
    @Beta
    interface Parent extends EffectiveStmtCtx {
        /**
         * {@return this statement's effective {@code config} value, {@code null} if not determined or applicable}
         */
        @Nullable Boolean effectiveConfig();

        // FIXME: 7.0.0: this is currently only used by AbstractTypeStatement
        @NonNull QNameModule effectiveNamespace();
    }

    /**
     * Minimum amount of state required to build an accurate effective view of a statement. This is a strict superset
     * of information available in {@link Parent}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement
     */
    @Beta
    non-sealed interface Current<A, D extends DeclaredStatement<A>>
            extends Parent, NamespaceStmtCtx, BoundStmtCtxCompat<A, D> {
        @Override
        StatementDefinition<A, D, ?> publicDefinition();

        @Override
        <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Current<X, Y> asDeclaring(
            StatementDefinition<X, Y, Z> def);

        @Override
        <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Current<X, Y> verifyDeclaring(
            StatementDefinition<X, Y, Z> def);

        @Override
        <X, Y extends DeclaredStatement<X>> @Nullable Current<X, Y> tryDeclaring(Class<Y> type);

        @NonNull QName moduleName();

        // FIXME: 8.0.0: this method should be moved to stmt.type in some shape or form
        @NonNull QName argumentAsTypeQName();
    }

    /**
     * A restricted version of {@link Current}, which does not expose the raw argument or the declared statement.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement
     */
    @Beta
    interface UndeclaredCurrent<A, D extends DeclaredStatement<A>> extends Current<A, D> {
        @Deprecated
        @Override
        default String rawArgument() {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        default D declared() {
            throw new UnsupportedOperationException();
        }
    }
}
