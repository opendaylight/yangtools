/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RevisionDateEffectiveStatementImpl;

public class RevisionDateStatementImpl extends AbstractDeclaredStatement<Revision> implements RevisionDateStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.REVISION_DATE).build();

    protected RevisionDateStatementImpl(final StmtContext<Revision, RevisionDateStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<Revision, RevisionDateStatement,
            EffectiveStatement<Revision, RevisionDateStatement>> {

        public Definition() {
            super(YangStmtMapping.REVISION_DATE);
        }

        @Override
        public Revision parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            try {
                return Revision.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new SourceException(ctx.getStatementSourceReference(), e,
                    "Revision value %s is not in required format yyyy-MM-dd", value);
            }
        }

        @Override
        public RevisionDateStatement createDeclared(final StmtContext<Revision, RevisionDateStatement, ?> ctx) {
            return new RevisionDateStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Revision, RevisionDateStatement> createEffective(final StmtContext<Revision,
                RevisionDateStatement, EffectiveStatement<Revision, RevisionDateStatement>> ctx) {
            return new RevisionDateEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Override
    public Revision getDate() {
        return argument();
    }
}
