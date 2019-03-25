/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllSchemaNode;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractVoidStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultDenyAllStatementSupport
        extends AbstractVoidStatementSupport<DefaultDenyAllStatement, DefaultDenyAllEffectiveStatement> {

    private static final class Declared extends AbstractDeclaredStatement<Void> implements DefaultDenyAllStatement {
        Declared(final StmtContext<Void, ?, ?> context) {
            super(context);
        }

        @Override
        public Void getArgument() {
            return null;
        }
    }

    private static final class Effective extends UnknownEffectiveStatementBase<Void, DefaultDenyAllStatement>
            implements DefaultDenyAllEffectiveStatement, DefaultDenyAllSchemaNode {

        private final SchemaPath path;

        Effective(final StmtContext<Void, DefaultDenyAllStatement, ?> ctx) {
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

    private static final DefaultDenyAllStatementSupport INSTANCE =
            new DefaultDenyAllStatementSupport(NACMStatements.DEFAULT_DENY_ALL);

    private final SubstatementValidator validator;

    private DefaultDenyAllStatementSupport(final StatementDefinition definition) {
        super(definition);
        this.validator = SubstatementValidator.builder(definition).build();
    }

    public static DefaultDenyAllStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public DefaultDenyAllStatement createDeclared(final StmtContext<Void, DefaultDenyAllStatement, ?> ctx) {
        return new Declared(ctx);
    }

    @Override
    public DefaultDenyAllEffectiveStatement createEffective(
            final StmtContext<Void, DefaultDenyAllStatement, DefaultDenyAllEffectiveStatement> ctx) {
        return new Effective(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }
}
