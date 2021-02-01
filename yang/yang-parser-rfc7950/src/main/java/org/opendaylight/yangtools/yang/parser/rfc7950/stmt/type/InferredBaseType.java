/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Intermediate holder for base type information used by {@link AbstractTypeStatementSupport}. It is propagated from
 * inference phase to effective statement build via {@link BaseTypeNamespace}.
 */
@NonNullByDefault
abstract class InferredBaseType {
    private static final class BuiltIn extends InferredBaseType {
        private final BuiltinEffectiveStatement baseType;

        BuiltIn(final Mutable<String, ?, ?> ctx, final BuiltinEffectiveStatement baseType) {
            super(ctx);
            this.baseType = requireNonNull(baseType);
        }

        @Override
        TypeEffectiveStatement<TypeStatement> toEffectiveType() {
            return baseType;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("baseType", baseType);
        }
    }

    private static final class Derived extends InferredBaseType {
        private final StmtContext<?, ?, ?> baseType;

        Derived(final Mutable<String, ?, ?> ctx, final StmtContext<?, ?, ?> baseType) {
            super(ctx);
            this.baseType = requireNonNull(baseType);
        }

        @Override
        TypeEffectiveStatement<TypeStatement> toEffectiveType() {
            return ((TypedefEffectiveStatement) baseType.buildEffective()).asTypeEffectiveStatement();
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("baseType", baseType);
        }
    }

    private final Mutable<String, ?, ?> ctx;

    InferredBaseType(final Mutable<String, ?, ?> ctx) {
        this.ctx = requireNonNull(ctx);
    }

    static InferredBaseType of(final Mutable<String, ?, ?> ctx, final BuiltinEffectiveStatement baseType) {
        return new BuiltIn(ctx, baseType);
    }

    static InferredBaseType of(final Mutable<String, ?, ?> ctx, final StmtContext<?, ?, ?> baseType) {
        return new Derived(ctx, baseType);
    }

    final QName resolveQName() {
        return ctx.bindEffectivePath(toEffectiveType().getTypeDefinition().getQName());
    }

    abstract TypeEffectiveStatement<TypeStatement> toEffectiveType();

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("statement", ctx);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }
}
