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
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultStatementSupport
        extends AbstractStringStatementSupport<DefaultStatement, DefaultEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.DEFAULT).build();
    private static final DefaultStatementSupport INSTANCE = new DefaultStatementSupport();

    private DefaultStatementSupport() {
        // Note: if we start interpreting the string we'll need to use StatementPolicy.declaredCopy()
        super(YangStmtMapping.DEFAULT, StatementPolicy.contextIndependent());
    }

    public static DefaultStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected DefaultStatement createDeclared(final StmtContext<String, DefaultStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createDefault(ctx.getRawArgument(), substatements);
    }

    @Override
    protected DefaultEffectiveStatement createEffective(final Current<String, DefaultStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createDefault(stmt.declared(), substatements);
    }
}