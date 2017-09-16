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
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumSpecificationEffectiveStatementImpl;

public class EnumSpecificationImpl extends AbstractDeclaredStatement<String> implements EnumSpecification {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPE)
            .addMultiple(YangStmtMapping.ENUM)
            .build();

    protected EnumSpecificationImpl(final StmtContext<String, EnumSpecification, ?> context) {
        super(context);
    }

    public static class Definition
            extends AbstractStatementSupport<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>> {

        public Definition() {
            super(YangStmtMapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public EnumSpecification createDeclared(final StmtContext<String, EnumSpecification, ?> ctx) {
            return new EnumSpecificationImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, EnumSpecification> createEffective(
                final StmtContext<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>> ctx) {
            return new EnumSpecificationEffectiveStatementImpl(ctx);
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
    public Collection<? extends EnumStatement> getEnums() {
        return allDeclared(EnumStatement.class);
    }
}
