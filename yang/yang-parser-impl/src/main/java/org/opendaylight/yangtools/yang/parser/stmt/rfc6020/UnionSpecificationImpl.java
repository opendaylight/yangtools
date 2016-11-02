/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UnionSpecificationEffectiveStatementImpl;

public class UnionSpecificationImpl extends AbstractDeclaredStatement<String>
        implements TypeStatement.UnionSpecification {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .TYPE)
            .addMultiple(Rfc6020Mapping.TYPE)
            .build();

    protected UnionSpecificationImpl(
            final StmtContext<String, TypeStatement.UnionSpecification, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String, TypeStatement.UnionSpecification, EffectiveStatement<String, TypeStatement.UnionSpecification>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public TypeStatement.UnionSpecification createDeclared(
                final StmtContext<String, TypeStatement.UnionSpecification, ?> ctx) {
            return new UnionSpecificationImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement.UnionSpecification> createEffective(
                final StmtContext<String, TypeStatement.UnionSpecification, EffectiveStatement<String, TypeStatement.UnionSpecification>> ctx) {
            return new UnionSpecificationEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<String, UnionSpecification, EffectiveStatement<String, UnionSpecification>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }

    @Nonnull
    @Override
    public Collection<? extends TypeStatement> getTypes() {
        return allDeclared(TypeStatement.class);
    }

}
