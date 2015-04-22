/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nullable;

public class UnknownStatementImpl extends AbstractDeclaredStatement<String> implements UnknownStatement<String> {

    protected UnknownStatementImpl(StmtContext<String, ?, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String, UnknownStatement<String>, EffectiveStatement<String, UnknownStatement<String>>> {


        public Definition(StatementDefinition publicDefinition) {
            super(publicDefinition);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return value;
        }

        @Override
        public UnknownStatement createDeclared(StmtContext<String, UnknownStatement<String>, ?> ctx) {
            return new UnknownStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, UnknownStatement<String>> createEffective(StmtContext<String, UnknownStatement<String>, EffectiveStatement<String, UnknownStatement<String>>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nullable
    @Override
    public String getArgument() {
        return rawArgument();
    }
}
