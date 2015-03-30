/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class SubmoduleStatementImpl extends
        AbstractRootStatement<SubmoduleStatement> implements SubmoduleStatement {

    protected SubmoduleStatementImpl(
            StmtContext<String, SubmoduleStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Submodule);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public SubmoduleStatement createDeclared(
                StmtContext<String, SubmoduleStatement, ?> ctx) {
            return new SubmoduleStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, SubmoduleStatement> createEffective(
                StmtContext<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> ctx) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onLinkageDeclared(
                Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> stmt)
                throws InferenceException, SourceException {

        }

    }

    @Override
    public String getName() {
        return rawArgument();
    }

    @Override
    public YangVersionStatement getYangVersion() {
        return firstDeclared(YangVersionStatement.class);
    }

    @Override
    public BelongsToStatement getBelongsTo() {
        return firstDeclared(BelongsToStatement.class);
    }

}
