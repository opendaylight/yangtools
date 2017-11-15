/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.prefix;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class PrefixStatementSupport
    extends AbstractStatementSupport<String, PrefixStatement, EffectiveStatement<String, PrefixStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .PREFIX)
        .build();
    private static final PrefixStatementSupport INSTANCE = new PrefixStatementSupport();

    private PrefixStatementSupport() {
        super(YangStmtMapping.PREFIX);
    }

    public static PrefixStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?,?> ctx, final String value) {
        return value;
    }

    @Override
    public PrefixStatement createDeclared(final StmtContext<String, PrefixStatement,?> ctx) {
        return new PrefixStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String,PrefixStatement> createEffective(
            final StmtContext<String, PrefixStatement, EffectiveStatement<String, PrefixStatement>> ctx) {
        return new PrefixEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}