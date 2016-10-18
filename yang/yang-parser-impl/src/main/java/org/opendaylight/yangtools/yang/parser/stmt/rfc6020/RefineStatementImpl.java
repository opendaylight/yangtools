/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RefineEffectiveStatementImpl;

public class RefineStatementImpl extends AbstractDeclaredStatement<SchemaNodeIdentifier> implements RefineStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .REFINE)
            .addOptional(Rfc6020Mapping.DEFAULT)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.CONFIG)
            .addOptional(Rfc6020Mapping.MANDATORY)
            .addOptional(Rfc6020Mapping.PRESENCE)
            .addAny(Rfc6020Mapping.MUST)
            .addOptional(Rfc6020Mapping.MIN_ELEMENTS)
            .addOptional(Rfc6020Mapping.MAX_ELEMENTS)
            .build();

    protected RefineStatementImpl(final StmtContext<SchemaNodeIdentifier, RefineStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<SchemaNodeIdentifier, RefineStatement, EffectiveStatement<SchemaNodeIdentifier, RefineStatement>> {

        public Definition() {
            super(Rfc6020Mapping.REFINE);
        }

        @Override
        public SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.nodeIdentifierFromPath(ctx, value);
        }

        @Override
        public RefineStatement createDeclared(final StmtContext<SchemaNodeIdentifier, RefineStatement, ?> ctx) {
            return new RefineStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<SchemaNodeIdentifier, RefineStatement> createEffective(
                final StmtContext<SchemaNodeIdentifier, RefineStatement, EffectiveStatement<SchemaNodeIdentifier, RefineStatement>> ctx) {
            return new RefineEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<SchemaNodeIdentifier, RefineStatement,
                EffectiveStatement<SchemaNodeIdentifier, RefineStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Override
    public String getTargetNode() {
        return rawArgument();
    }

    @Nullable
    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Nullable
    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }
}
