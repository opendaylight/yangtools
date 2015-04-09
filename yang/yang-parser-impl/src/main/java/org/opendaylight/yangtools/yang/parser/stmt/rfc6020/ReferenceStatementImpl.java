/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import javax.annotation.Nonnull;

public class ReferenceStatementImpl extends AbstractDeclaredStatement<String> implements ReferenceStatement {

    protected ReferenceStatementImpl(
            StmtContext<String, ReferenceStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,ReferenceStatement,EffectiveStatement<String,ReferenceStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Reference);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return value;
        }

        @Override
        public ReferenceStatement createDeclared(StmtContext<String, ReferenceStatement, ?> ctx) {
            return new ReferenceStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, ReferenceStatement> createEffective(StmtContext<String, ReferenceStatement, EffectiveStatement<String, ReferenceStatement>> ctx) {
            return new ReferenceEffectiveStatementImpl(ctx);
        }
    }

    @Nonnull @Override
    public String getText() {
        return rawArgument();
    }
}
