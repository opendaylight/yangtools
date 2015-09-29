/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeviateEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;

public class DeviateStatementImpl extends AbstractDeclaredStatement<Deviation.Deviate> implements DeviateStatement {

    protected DeviateStatementImpl(
            StmtContext<Deviation.Deviate, DeviateStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<Deviation.Deviate,DeviateStatement,EffectiveStatement<Deviation.Deviate,DeviateStatement>> {

        public Definition() {
            super(Rfc6020Mapping.DEVIATE);
        }

        @Override public Deviation.Deviate parseArgumentValue(
                StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return Utils.parseDeviateFromString(value);
        }

        @Override public DeviateStatement createDeclared(
                StmtContext<Deviation.Deviate, DeviateStatement, ?> ctx) {
            return new DeviateStatementImpl(ctx);
        }

        @Override public EffectiveStatement<Deviation.Deviate, DeviateStatement> createEffective(
                StmtContext<Deviation.Deviate, DeviateStatement, EffectiveStatement<Deviation.Deviate, DeviateStatement>> ctx) {
            return new DeviateEffectiveStatementImpl(ctx);
        }

    }

    @Nonnull @Override
    public Deviation.Deviate getValue() {
        return argument();
    }
}
