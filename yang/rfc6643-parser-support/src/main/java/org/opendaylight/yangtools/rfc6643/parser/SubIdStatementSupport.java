/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class SubIdStatementSupport
        extends AbstractStatementSupport<Integer, SubIdStatement, SubIdEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.SUB_ID).build();
    private static final SubIdStatementSupport INSTANCE = new SubIdStatementSupport();

    private SubIdStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.SUB_ID);
    }

    public static SubIdStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return Integer.parseUnsignedInt(value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    public SubIdStatement createDeclared(final StmtContext<Integer, SubIdStatement, ?> ctx) {
        return new SubIdStatementImpl(ctx);
    }

    @Override
    public SubIdEffectiveStatement createEffective(
            final StmtContext<Integer, SubIdStatement, SubIdEffectiveStatement> ctx) {
        return new SubIdEffectiveStatementImpl(ctx);
    }
}
