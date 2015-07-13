/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Status;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class StatusStatementImpl extends AbstractDeclaredStatement<Status>
        implements StatusStatement {

    protected StatusStatementImpl(
            StmtContext<Status, StatusStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Status, StatusStatement, EffectiveStatement<Status, StatusStatement>> {

        public Definition() {
            super(Rfc6020Mapping.STATUS);
        }

        @Override
        public Status parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.parseStatus(value);
        }

        @Override
        public StatusStatement createDeclared(
                StmtContext<Status, StatusStatement, ?> ctx) {
            return new StatusStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Status, StatusStatement> createEffective(
                StmtContext<Status, StatusStatement, EffectiveStatement<Status, StatusStatement>> ctx) {
            return new StatusEffectiveStatementImpl(ctx);
        }
    }

    @Override
    public Status getValue() {
        return argument();
    }

}
