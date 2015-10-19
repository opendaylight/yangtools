/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.MandatoryEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import javax.annotation.Nonnull;

public class MandatoryStatementImpl extends AbstractDeclaredStatement<Boolean> implements
        MandatoryStatement {
    private static final long serialVersionUID = 1L;

    protected MandatoryStatementImpl(
            StmtContext<Boolean, MandatoryStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<Boolean,MandatoryStatement,EffectiveStatement<Boolean,MandatoryStatement>> {

        public Definition() {
            super(Rfc6020Mapping.MANDATORY);
        }

        @Override public Boolean parseArgumentValue(
                StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return Boolean.valueOf(value);
        }

        @Override public MandatoryStatement createDeclared(
                StmtContext<Boolean, MandatoryStatement, ?> ctx) {
            return new MandatoryStatementImpl(ctx);
        }

        @Override public EffectiveStatement<Boolean, MandatoryStatement> createEffective(
                StmtContext<Boolean, MandatoryStatement, EffectiveStatement<Boolean, MandatoryStatement>> ctx) {
            return new MandatoryEffectiveStatementImpl(ctx);
        }
    }

    @Nonnull @Override
    public Boolean getValue() {
        return argument();
    }
}
