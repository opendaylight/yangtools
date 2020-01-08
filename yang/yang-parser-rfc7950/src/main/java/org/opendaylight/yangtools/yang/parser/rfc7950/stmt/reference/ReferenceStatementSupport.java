/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.reference;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ReferenceStatementSupport
        extends BaseStringStatementSupport<ReferenceStatement, ReferenceEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.REFERENCE)
        .build();
    private static final ReferenceStatementSupport INSTANCE = new ReferenceStatementSupport();

    private ReferenceStatementSupport() {
        super(YangStmtMapping.REFERENCE);
    }

    public static ReferenceStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public ReferenceStatement createDeclared(final StmtContext<String, ReferenceStatement, ?> ctx) {
        return new ReferenceStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ReferenceEffectiveStatement createEffective(
            final StmtContext<String, ReferenceStatement, ReferenceEffectiveStatement> ctx,
            final ReferenceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularReferenceEffectiveStatement(declared, substatements);
    }

    @Override
    protected ReferenceEffectiveStatement createEmptyEffective(
            final StmtContext<String, ReferenceStatement, ReferenceEffectiveStatement> ctx,
            final ReferenceStatement declared) {
        return new EmptyReferenceEffectiveStatement(declared);
    }
}