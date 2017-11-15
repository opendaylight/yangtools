/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yin_element;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class YinElementStatementSupport extends
        AbstractStatementSupport<Boolean, YinElementStatement, EffectiveStatement<Boolean, YinElementStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.YIN_ELEMENT).build();
    private static final YinElementStatementSupport INSTANCE = new YinElementStatementSupport();

    private YinElementStatementSupport() {
        super(YangStmtMapping.YIN_ELEMENT);
    }

    public static YinElementStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Boolean parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.parseBoolean(ctx, value);
    }

    @Override
    public YinElementStatement createDeclared(final StmtContext<Boolean, YinElementStatement, ?> ctx) {
        return new YinElementStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<Boolean, YinElementStatement> createEffective(
            final StmtContext<Boolean, YinElementStatement, EffectiveStatement<Boolean, YinElementStatement>> ctx) {
        return new YinElementEffectiveStatementImpl(ctx);
    }

    @Override
    public String internArgument(final String rawArgument) {
        return ArgumentUtils.internBoolean(rawArgument);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}