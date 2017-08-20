/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.text.ParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RevisionEffectiveStatementImpl;

public class RevisionStatementImpl extends AbstractDeclaredStatement<Revision>
        implements RevisionStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.REVISION)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .build();

    protected RevisionStatementImpl(final StmtContext<Revision, RevisionStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Revision, RevisionStatement, EffectiveStatement<Revision, RevisionStatement>> {

        public Definition() {
            super(YangStmtMapping.REVISION);
        }

        @Override
        public Revision parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            try {
                return Revision.forString(value);
            } catch (ParseException e) {
                throw new SourceException(ctx.getStatementSourceReference(), e,
                    "Revision value %s is not in required format yyyy-MM-dd", value);
            }
        }

        @Override
        public RevisionStatement createDeclared(final StmtContext<Revision, RevisionStatement, ?> ctx) {
            return new RevisionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Revision, RevisionStatement> createEffective(
                final StmtContext<Revision, RevisionStatement, EffectiveStatement<Revision, RevisionStatement>> ctx) {
            return new RevisionEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public Revision getDate() {
        return argument();
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
