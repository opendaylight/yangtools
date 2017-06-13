/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.InstanceIdentifierSpecificationEffectiveStatementImpl;

public class InstanceIdentifierSpecificationImpl extends
        AbstractDeclaredStatement<String> implements InstanceIdentifierSpecification {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPE)
            .addOptional(YangStmtMapping.REQUIRE_INSTANCE)
            .build();

    protected InstanceIdentifierSpecificationImpl(
            final StmtContext<String, InstanceIdentifierSpecification, ?> ctx) {
        super(ctx);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, InstanceIdentifierSpecification, EffectiveStatement<String, InstanceIdentifierSpecification>> {

        public Definition() {
            super(YangStmtMapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public InstanceIdentifierSpecification createDeclared(
                final StmtContext<String, InstanceIdentifierSpecification, ?> ctx) {
            return new InstanceIdentifierSpecificationImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, InstanceIdentifierSpecification> createEffective(
                final StmtContext<String, InstanceIdentifierSpecification, EffectiveStatement<String, InstanceIdentifierSpecification>> ctx) {
            return new InstanceIdentifierSpecificationEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }

    @Override
    public RequireInstanceStatement getRequireInstance() {
        return firstDeclared(RequireInstanceStatement.class);
    }

}
