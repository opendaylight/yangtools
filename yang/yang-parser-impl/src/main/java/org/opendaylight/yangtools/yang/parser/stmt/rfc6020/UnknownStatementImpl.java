/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.common.QName;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

public class UnknownStatementImpl extends AbstractDeclaredStatement<QName> implements UnknownStatement<QName> {

    protected UnknownStatementImpl(final StmtContext<QName, ?, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, UnknownStatement<QName>, EffectiveStatement<QName, UnknownStatement<QName>>> {

        public Definition(final StatementDefinition publicDefinition) {
            super(publicDefinition);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) throws SourceException {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public UnknownStatement<QName> createDeclared(final StmtContext<QName, UnknownStatement<QName>, ?> ctx) {
            return new UnknownStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, UnknownStatement<QName>> createEffective(
                final StmtContext<QName, UnknownStatement<QName>, EffectiveStatement<QName, UnknownStatement<QName>>> ctx) {
            return new UnknownEffectiveStatementImpl(ctx);
        }
    }

    @Nullable
    @Override
    public QName getArgument() {
        return argument();
    }
}
