/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.BelongsEffectiveToStatementImpl;

import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class BelongsToStatementImpl extends AbstractDeclaredStatement<String>
        implements BelongsToStatement {

    protected BelongsToStatementImpl(
            StmtContext<String, BelongsToStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> {

        public Definition() {
            super(Rfc6020Mapping.BELONGS_TO);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public BelongsToStatement createDeclared(
                StmtContext<String, BelongsToStatement, ?> ctx) {
            return new BelongsToStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, BelongsToStatement> createEffective(
                StmtContext<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> ctx) {
            return new BelongsEffectiveToStatementImpl(ctx);
        }

    }

    @Override
    public String getModule() {
        return argument();
    }

    @Override
    public PrefixStatement getPrefix() {
        return firstDeclared(PrefixStatement.class);
    }

}
