/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractVoidStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

abstract class AbstractHashedValueStatementSupport
        extends AbstractVoidStatementSupport<OpenConfigHashedValueStatement,
            OpenConfigHashedValueEffectiveStatement> {

    private static final class Declared extends AbstractDeclaredStatement<Void>
        implements OpenConfigHashedValueStatement {
        Declared(final StmtContext<Void, ?, ?> context) {
            super(context);
        }

        @Override
        public Void getArgument() {
            return null;
        }
    }

    private static final class Effective
            extends UnknownEffectiveStatementBase<Void, OpenConfigHashedValueStatement>
            implements OpenConfigHashedValueEffectiveStatement {

        private final SchemaPath path;

        Effective(final StmtContext<Void, OpenConfigHashedValueStatement, ?> ctx) {
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

    private final SubstatementValidator validator;

    AbstractHashedValueStatementSupport(final StatementDefinition definition) {
        super(definition);
        this.validator = SubstatementValidator.builder(definition).build();
    }

    @Override
    public final OpenConfigHashedValueStatement createDeclared(
            final StmtContext<Void, OpenConfigHashedValueStatement, ?> ctx) {
        return new Declared(ctx);
    }

    @Override
    public final OpenConfigHashedValueEffectiveStatement createEffective(
            final StmtContext<Void, OpenConfigHashedValueStatement, OpenConfigHashedValueEffectiveStatement> ctx) {
        return new Effective(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }
}
