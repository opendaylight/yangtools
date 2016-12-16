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
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LeafrefSpecificationEffectiveStatementImpl;

public class LeafrefSpecificationImpl extends AbstractDeclaredStatement<String>
        implements TypeStatement.LeafrefSpecification {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPE)
            .addMandatory(YangStmtMapping.PATH)
            .addOptional(YangStmtMapping.REQUIRE_INSTANCE)
            .build();

    protected LeafrefSpecificationImpl(
            final StmtContext<String, TypeStatement.LeafrefSpecification, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, TypeStatement.LeafrefSpecification, EffectiveStatement<String, TypeStatement.LeafrefSpecification>> {

        public Definition() {
            super(YangStmtMapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public TypeStatement.LeafrefSpecification createDeclared(
                final StmtContext<String, TypeStatement.LeafrefSpecification, ?> ctx) {
            return new LeafrefSpecificationImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement.LeafrefSpecification> createEffective(
                final StmtContext<String, TypeStatement.LeafrefSpecification, EffectiveStatement<String, TypeStatement.LeafrefSpecification>> ctx) {
            return new LeafrefSpecificationEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<String, LeafrefSpecification,
                EffectiveStatement<String, LeafrefSpecification>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            getSubstatementValidator().validate(stmt);
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

    @Nonnull
    @Override
    public PathStatement getPath() {
        return firstDeclared(PathStatement.class);
    }

}
