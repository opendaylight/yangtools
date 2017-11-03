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
import org.opendaylight.yangtools.rfc6536.model.api.NACMConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementBase;

final class DefaultDenyWrite
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
            path = ctx.getParentContext().getSchemaPath().get().createChild(QNAME);
        }

        @Override
        public QName getQName() {
            return QNAME;
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }
    }

    private static final QName QNAME = QName.create(NACMConstants.RFC6536_MODULE, "default-deny-write").intern();
    private static final StatementDefinition DEFINITION = new StatementDefinition() {
        @Override
        public boolean isArgumentYinElement() {
            return false;
        }

        @Override
        public QName getStatementName() {
            return QNAME;
        }

        @Override
        public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
            return DefaultDenyWriteEffectiveStatement.class;
        }

        @Override
        public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
            return DefaultDenyWriteStatement.class;
        }

        @Override
        public QName getArgumentName() {
            return null;
        }
    };
    private static final DefaultDenyWrite INSTANCE = new DefaultDenyWrite();

    private final SubstatementValidator validator;

    DefaultDenyWrite() {
        super(DEFINITION);
        this.validator = SubstatementValidator.builder(DEFINITION).build();
    }

    static DefaultDenyWrite getInstance() {
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
