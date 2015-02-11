/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Supplier;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;



public interface ModelInferenceActionBuilder {

    public interface Prereq<T> extends Supplier<T> {

        @Override
        public T get();

        boolean isDone();

    }

    public interface InferenceAction {

        void apply() throws InferenceException;

        void preconditionsWasNotMet(Iterable<Prereq<?>> failed) throws InferenceException;
    }


    <D extends DeclaredStatement<?>> Prereq<D> requiresDeclared(StmtContext<?,? extends D,?> context);
    <K,D extends DeclaredStatement<?>,N extends StatementNamespace<? super K, D, ?>> Prereq<D> requiresDeclared(StmtContext<?,?,?> context,Class<N> namespace, K key);
    <K,D extends DeclaredStatement<?>,N extends StatementNamespace<? super K, D, ?>> Prereq<StmtContext<?,D,?>> requiresDeclaredCtx(StmtContext<?,?,?> context,Class<N> namespace, K key);

    <E extends EffectiveStatement<?,?>> Prereq<E> requiresEffective(StmtContext<?,?,? extends E> stmt);
    <K,E extends EffectiveStatement<?,?>,N extends StatementNamespace<? super K, ?, E>> Prereq<E> requiresEffective(StmtContext<?,?,?> context,Class<N> namespace, K key);
    <K,E extends EffectiveStatement<?,?>,N extends StatementNamespace<? super K, ?, E>> Prereq<StmtContext<?,?,E>> requiresEffectiveCtx(StmtContext<?,?,?> context,Class<N> namespace, K key);

    <N extends IdentifierNamespace<? ,?>> Prereq<Mutable<?,?,?>> mutatesNamespace(Mutable<?,?, ?> ctx, Class<N> namespace);
    <T extends Mutable<?,?,?>> Prereq<T> mutatesEffectiveCtx(T stmt);
    <K,E extends EffectiveStatement<?,?>,N extends StatementNamespace<? super K, ?, ? extends E>> Prereq<Mutable<?,?,E>> mutatesEffectiveCtx(StmtContext<?,?,?> context,Class<N> namespace, K key);

    void apply(InferenceAction runnable);
}