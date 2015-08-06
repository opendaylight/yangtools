/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ConfigEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import javax.annotation.Nonnull;

public class ConfigStatementImpl extends AbstractDeclaredStatement<Boolean> implements ConfigStatement {

    protected ConfigStatementImpl(
            StmtContext<Boolean, ConfigStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<Boolean,ConfigStatement,EffectiveStatement<Boolean,ConfigStatement>> {

        public Definition() {
            super(Rfc6020Mapping.CONFIG);
        }

        @Override public Boolean parseArgumentValue(StmtContext<?, ?, ?> ctx,
                String value) throws SourceException {
            return Boolean.valueOf(value);
        }

        @Override public ConfigStatement createDeclared(StmtContext<Boolean, ConfigStatement, ?> ctx) {
            return new ConfigStatementImpl(ctx);
        }

        @Override public EffectiveStatement<Boolean, ConfigStatement> createEffective(StmtContext<Boolean, ConfigStatement, EffectiveStatement<Boolean, ConfigStatement>> ctx) {
            return new ConfigEffectiveStatementImpl(ctx);
        }
    }

    @Nonnull @Override
    public Boolean getValue() {
        return argument();
    }
}
