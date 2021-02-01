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
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Intermediate holder for base type information used by {@link AbstractTypeStatementSupport}. It is propagated from
 * inference phase to effective statement build via {@link BaseTypeNamespace}.
 */
@NonNullByDefault
abstract class InferredBaseType implements Immutable {
    private static final class BuiltIn extends InferredBaseType {
        private final BuiltinEffectiveStatement statement;

        BuiltIn(final QName qname, final BuiltinEffectiveStatement statement) {
            super(qname);
            this.statement = requireNonNull(statement);
        }

        @Override
        TypeEffectiveStatement<TypeStatement> toEffectiveType() {
            return statement;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("statement", statement);
        }
    }

    private static final class Derived extends InferredBaseType {
        private final StmtContext<?, ?, ?> ctx;

        Derived(final QName qname, final StmtContext<?, ?, ?> ctx) {
            super(qname);
            this.ctx = requireNonNull(ctx);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("context", ctx);
        }

        @Override
        TypeEffectiveStatement<TypeStatement> toEffectiveType() {
            return ((TypedefEffectiveStatement) ctx.buildEffective()).asTypeEffectiveStatement();
        }
    }


    private final QName qname;

    InferredBaseType(final QName qname) {
        this.qname = requireNonNull(qname);
    }

    static InferredBaseType of(final QName qname, final BuiltinEffectiveStatement statement) {
        return new BuiltIn(qname, statement);
    }

    static InferredBaseType of(final QName qname, final StmtContext<?, ?, ?> ctx) {
        return new Derived(qname, ctx);
    }

    final QName qname() {
        return qname;
    }

    abstract TypeEffectiveStatement<TypeStatement> toEffectiveType();

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("qname", qname);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }
}
