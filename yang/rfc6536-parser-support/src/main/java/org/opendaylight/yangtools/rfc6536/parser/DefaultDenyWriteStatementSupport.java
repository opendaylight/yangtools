/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultDenyWriteStatementSupport
    extends AbstractStatementSupport<Void, DefaultDenyWriteStatement, DefaultDenyWriteEffectiveStatement> {

    private static final class Declared extends AbstractDeclaredStatement<Void> implements DefaultDenyWriteStatement {
        Declared(final StmtContext<Void, ?, ?> context) {
            super(context);
        }

        @Override
        public Void getArgument() {
            return null;
        }
    }

    private static final class Effective extends UnknownEffectiveStatementBase<Void, DefaultDenyWriteStatement>
            implements DefaultDenyWriteEffectiveStatement {

        private final SchemaPath path;

        Effective(final StmtContext<Void, DefaultDenyWriteStatement, ?> ctx) {
            super(ctx);
            path = ctx.coerceParentContext().getSchemaPath().get().createChild(
                ctx.getPublicDefinition().getStatementName());
        }

        @Override
        public QName getQName() {
            return path.getLastComponent();
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }
    }

    private static final DefaultDenyWriteStatementSupport INSTANCE =
            new DefaultDenyWriteStatementSupport(NACMStatements.DEFAULT_DENY_WRITE);

    private final SubstatementValidator validator;

    DefaultDenyWriteStatementSupport(final StatementDefinition definition) {
        super(definition);
        this.validator = SubstatementValidator.builder(definition).build();
    }

    public static DefaultDenyWriteStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public DefaultDenyWriteStatement createDeclared(final StmtContext<Void, DefaultDenyWriteStatement, ?> ctx) {
        return new Declared(ctx);
    }

    @Override
    public DefaultDenyWriteEffectiveStatement createEffective(
            final StmtContext<Void, DefaultDenyWriteStatement, DefaultDenyWriteEffectiveStatement> ctx) {
        return new Effective(ctx);
    }

    @Override
    public Void parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return null;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }
}
