/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision;

import com.google.common.collect.ImmutableList;
import java.time.format.DateTimeParseException;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class RevisionStatementSupport
        extends BaseStatementSupport<Revision, RevisionStatement, RevisionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.REVISION)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .build();
    private static final RevisionStatementSupport INSTANCE = new RevisionStatementSupport();

    private RevisionStatementSupport() {
        super(YangStmtMapping.REVISION);
    }

    public static RevisionStatementSupport getInstance() {
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
    protected RevisionStatement createDeclared(final StmtContext<Revision, RevisionStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularRevisionStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected RevisionStatement createEmptyDeclared(final StmtContext<Revision, RevisionStatement, ?> ctx) {
        return new EmptyRevisionStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected RevisionEffectiveStatement createEffective(
            final StmtContext<Revision, RevisionStatement, RevisionEffectiveStatement> ctx,
            final RevisionStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularRevisionEffectiveStatement(declared, substatements);
    }

    @Override
    protected RevisionEffectiveStatement createEmptyEffective(
            final StmtContext<Revision, RevisionStatement, RevisionEffectiveStatement> ctx,
            final RevisionStatement declared) {
        return new EmptyRevisionEffectiveStatement(declared);
    }
}