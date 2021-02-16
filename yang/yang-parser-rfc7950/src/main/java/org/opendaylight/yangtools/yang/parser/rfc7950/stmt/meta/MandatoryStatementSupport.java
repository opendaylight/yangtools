/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractBooleanStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class MandatoryStatementSupport extends
        AbstractBooleanStatementSupport<MandatoryStatement, MandatoryEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.MANDATORY).build();
    private static final MandatoryStatementSupport INSTANCE = new MandatoryStatementSupport();

    private MandatoryStatementSupport() {
        super(YangStmtMapping.MANDATORY,
            EffectiveStatements.createMandatory(DeclaredStatements.createMandatory(Boolean.FALSE)),
            EffectiveStatements.createMandatory(DeclaredStatements.createMandatory(Boolean.TRUE)),
            StatementPolicy.contextIndependent());
    }

    public static MandatoryStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected MandatoryStatement createDeclared(final StmtContext<Boolean, MandatoryStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createMandatory(ctx.getArgument(), substatements);
    }

    @Override
    protected MandatoryEffectiveStatement createEffective(final MandatoryStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createMandatory(declared, substatements);
    }

    @Override
    protected MandatoryEffectiveStatement createEmptyEffective(final MandatoryStatement declared) {
        return EffectiveStatements.createMandatory(declared);
    }
}
