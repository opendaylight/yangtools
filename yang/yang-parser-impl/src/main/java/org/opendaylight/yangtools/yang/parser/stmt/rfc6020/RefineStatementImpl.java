/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
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
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .REFINE)
            .addOptional(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.CONFIG)
            .addOptional(YangStmtMapping.MANDATORY)
            .addOptional(YangStmtMapping.PRESENCE)
            .addAny(YangStmtMapping.MUST)
            .addOptional(YangStmtMapping.MIN_ELEMENTS)
            .addOptional(YangStmtMapping.MAX_ELEMENTS)
            .build();

    protected RefineStatementImpl(final StmtContext<SchemaNodeIdentifier, RefineStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<SchemaNodeIdentifier, RefineStatement, EffectiveStatement<SchemaNodeIdentifier, RefineStatement>> {

        public Definition() {
            super(YangStmtMapping.REFINE);
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
            getSubstatementValidator().validate(stmt);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
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
