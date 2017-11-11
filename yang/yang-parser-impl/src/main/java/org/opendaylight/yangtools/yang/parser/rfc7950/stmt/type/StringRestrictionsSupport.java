/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.StringRestrictions;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

//FIXME: this class is not used anywhere, decide its future
final class StringRestrictionsSupport
        extends AbstractStatementSupport<String, StringRestrictions, EffectiveStatement<String, StringRestrictions>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .TYPE)
        .addOptional(YangStmtMapping.LENGTH)
        .addAny(YangStmtMapping.PATTERN)
        .build();

    StringRestrictionsSupport() {
        super(YangStmtMapping.TYPE);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public StringRestrictions createDeclared(final StmtContext<String, StringRestrictions, ?> ctx) {
        return new StringRestrictionsImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, StringRestrictions> createEffective(
            final StmtContext<String, StringRestrictions, EffectiveStatement<String, StringRestrictions>> ctx) {
        return new StringRestrictionsEffectiveStatement(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}