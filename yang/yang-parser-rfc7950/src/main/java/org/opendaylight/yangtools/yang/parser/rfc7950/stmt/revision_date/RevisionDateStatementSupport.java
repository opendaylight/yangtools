/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision_date;

import com.google.common.collect.ImmutableList;
import java.time.format.DateTimeParseException;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class RevisionDateStatementSupport
        extends BaseStatementSupport<Revision, RevisionDateStatement, RevisionDateEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.REVISION_DATE).build();
    private static final RevisionDateStatementSupport INSTANCE = new RevisionDateStatementSupport();

    private RevisionDateStatementSupport() {
        super(YangStmtMapping.REVISION_DATE, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static RevisionDateStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Revision parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Revision.of(value);
        } catch (DateTimeParseException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e,
                "Revision value %s is not in required format yyyy-MM-dd", value);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected RevisionDateStatement createDeclared(final StmtContext<Revision, RevisionDateStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularRevisionDateStatement(ctx, substatements);
    }

    @Override
    protected RevisionDateStatement createEmptyDeclared(final StmtContext<Revision, RevisionDateStatement, ?> ctx) {
        return new EmptyRevisionDateStatement(ctx);
    }

    @Override
    protected RevisionDateEffectiveStatement createEffective(
            final StmtContext<Revision, RevisionDateStatement, RevisionDateEffectiveStatement> ctx,
            final RevisionDateStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularRevisionDateEffectiveStatement(declared, substatements);
    }

    @Override
    protected RevisionDateEffectiveStatement createEmptyEffective(
            final StmtContext<Revision, RevisionDateStatement, RevisionDateEffectiveStatement> ctx,
            final RevisionDateStatement declared) {
        return new EmptyRevisionDateEffectiveStatement(declared);
    }
}