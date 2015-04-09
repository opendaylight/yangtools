/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class StatusStatementImpl extends AbstractDeclaredStatement<String>
        implements StatusStatement {

    protected StatusStatementImpl(
            StmtContext<String, StatusStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, StatusStatement, EffectiveStatement<String, StatusStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Status);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public StatusStatement createDeclared(
                StmtContext<String, StatusStatement, ?> ctx) {
            return new StatusStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, StatusStatement> createEffective(
                StmtContext<String, StatusStatement, EffectiveStatement<String, StatusStatement>> ctx) {
            return new StatusEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public String getValue() {
        return argument();
    }

}
