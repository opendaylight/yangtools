/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ExtendedTypeEffectiveStatementImpl;

public class TypeStatementImpl extends AbstractDeclaredStatement<String>
        implements TypeStatement {

    protected TypeStatementImpl(final StmtContext<String, TypeStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, TypeStatement, EffectiveStatement<String, TypeStatement>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public TypeStatement createDeclared(final StmtContext<String, TypeStatement, ?> ctx) {
            return new TypeStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement> createEffective(
                final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

            /*
             * If this type definition does not include any other statements, we do not want to generate a new effective
             * statement, as that would mean we have an object for every occurrence of the 'type' statement.
             *
             * Let us look up the typedef statement to which this type statement refers and let's get the effective
             * type statement from there. This was the typedef statement acts as an interner and all type statements
             * which end up point to it without modification will use the same type effective statement
             */
            if (ctx.declaredSubstatements().isEmpty()) {
                final QName qname = Utils.qNameFromArgument(ctx, ctx.getStatementArgument());
                final StmtContext<?, TypedefStatement, TypedefEffectiveStatement> typedef =
                        ctx.getFromNamespace(TypeNamespace.class, qname);
                if (typedef != null) {
                    return typedef.buildEffective().getEffectiveTypeStatement();
                }

                // union, leafref, identityref and decimal64,
                // FIXME: these should by a different class due to the special-case
                return new ExtendedTypeEffectiveStatementImpl(ctx, false);
            }

            // FIXME: improve the test of isExtended - e.g. unknown statements, etc..
            return new ExtendedTypeEffectiveStatementImpl(ctx, true);
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }
}
