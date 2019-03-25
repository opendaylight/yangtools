/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesEffectiveStatement;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesSchemaNode;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesStatement;
import org.opendaylight.yangtools.rfc6241.model.api.NETCONFStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractVoidStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class GetFilterElementAttributesStatementSupport
    extends AbstractVoidStatementSupport<GetFilterElementAttributesStatement,
        GetFilterElementAttributesEffectiveStatement> {

    private static final class Declared extends AbstractDeclaredStatement<Void>
            implements GetFilterElementAttributesStatement {
        Declared(final StmtContext<Void, ?, ?> context) {
            super(context);
        }

        @Override
        public Void getArgument() {
            return null;
        }
    }

    private static final class Effective
            extends UnknownEffectiveStatementBase<Void, GetFilterElementAttributesStatement>
            implements GetFilterElementAttributesEffectiveStatement, GetFilterElementAttributesSchemaNode {

        private final SchemaPath path;

        Effective(final StmtContext<Void, GetFilterElementAttributesStatement, ?> ctx) {
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

    private static final GetFilterElementAttributesStatementSupport INSTANCE =
            new GetFilterElementAttributesStatementSupport(NETCONFStatements.GET_FILTER_ELEMENT_ATTRIBUTES);

    private final SubstatementValidator validator;

    GetFilterElementAttributesStatementSupport(final StatementDefinition definition) {
        super(definition);
        this.validator = SubstatementValidator.builder(definition).build();
    }

    public static GetFilterElementAttributesStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public GetFilterElementAttributesStatement createDeclared(
            final StmtContext<Void, GetFilterElementAttributesStatement, ?> ctx) {
        return new Declared(ctx);
    }

    @Override
    public GetFilterElementAttributesEffectiveStatement createEffective(
            final StmtContext<Void, GetFilterElementAttributesStatement,
                GetFilterElementAttributesEffectiveStatement> ctx) {
        return new Effective(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }
}
