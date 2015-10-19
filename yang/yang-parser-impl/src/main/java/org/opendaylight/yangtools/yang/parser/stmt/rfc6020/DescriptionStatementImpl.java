/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import javax.annotation.Nonnull;

public class DescriptionStatementImpl extends AbstractDeclaredStatement<String> implements DescriptionStatement {
    private static final long serialVersionUID = 1L;

    protected DescriptionStatementImpl(
            StmtContext<String, DescriptionStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,DescriptionStatement,EffectiveStatement<String,DescriptionStatement>> {

        public Definition() {
            super(Rfc6020Mapping.DESCRIPTION);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return value;
        }

        @Override
        public DescriptionStatement createDeclared(StmtContext<String, DescriptionStatement, ?> ctx) {
            return new DescriptionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, DescriptionStatement> createEffective(StmtContext<String, DescriptionStatement, EffectiveStatement<String, DescriptionStatement>> ctx) {
            return new DescriptionEffectiveStatementImpl(ctx);
        }
    }

    @Nonnull @Override
    public String getText() {
        return rawArgument();
    }
}
