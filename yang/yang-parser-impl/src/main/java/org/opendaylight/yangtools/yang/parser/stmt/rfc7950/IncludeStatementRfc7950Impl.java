/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.IncludeStatementImpl;

public class IncludeStatementRfc7950Impl extends IncludeStatementImpl {

    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator
            .builder(Rfc6020Mapping.INCLUDE)
            .addOptional(Rfc6020Mapping.REVISION_DATE)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addOptional(Rfc6020Mapping.REFERENCE).build();

    protected IncludeStatementRfc7950Impl(StmtContext<String, IncludeStatement, ?> context) {
        super(context);
    }

    public static class Definition extends IncludeStatementImpl.Definition {

        @Override
        public IncludeStatement createDeclared(final StmtContext<String, IncludeStatement, ?> ctx) {
            return new IncludeStatementRfc7950Impl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<String, IncludeStatement,
                EffectiveStatement<String, IncludeStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }
}
