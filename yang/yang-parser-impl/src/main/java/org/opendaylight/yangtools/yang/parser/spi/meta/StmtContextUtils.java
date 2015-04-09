/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Function;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public final class StmtContextUtils {

    private static final Function<StmtContext<?, ?,?>, DeclaredStatement<?>> BUILD_DECLARED = new Function<StmtContext<?,?,?>, DeclaredStatement<?>>() {
        @Override
        public DeclaredStatement<?> apply(StmtContext<?,?,?> input) {
            return input.buildDeclared();
        }
    };

    private static final Function<StmtContext<?, ?,?>, EffectiveStatement<?,?>> BUILD_EFFECTIVE = new Function<StmtContext<?,?,?>, EffectiveStatement<?,?>>() {
        @Override
        public EffectiveStatement<?,?> apply(StmtContext<?,?,?> input) {
            return input.buildEffective();
        }
    };

    private StmtContextUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SuppressWarnings("unchecked")
    public static <D extends DeclaredStatement<?>> Function<StmtContext<?, ? extends D, ?>, D> buildDeclared() {
        return Function.class.cast(BUILD_DECLARED);
    }

    @SuppressWarnings("unchecked")
    public static <E extends EffectiveStatement<?,?>> Function<StmtContext<?, ?,? extends E>, E> buildEffective() {
        return Function.class.cast(BUILD_EFFECTIVE);
    }

    @SuppressWarnings("unchecked")
    public static <AT,DT extends DeclaredStatement<AT>> AT firstAttributeOf(Iterable<? extends StmtContext<?,?,?>> contexts, Class<DT> declaredType) {
        for(StmtContext<?, ?, ?> ctx : contexts) {
            if(producesDeclared(ctx,declaredType)) {
                return (AT) ctx.getStatementArgument();
            }
        }
        return null;
    }

    public static boolean producesDeclared(StmtContext<?, ?, ?> ctx, Class<? extends DeclaredStatement<?>> type) {
        return type.isAssignableFrom(ctx.getPublicDefinition().getDeclaredRepresentationClass());
    }
}
