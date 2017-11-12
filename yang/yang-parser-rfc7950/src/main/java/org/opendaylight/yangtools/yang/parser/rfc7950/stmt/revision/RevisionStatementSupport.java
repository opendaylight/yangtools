/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision;

import java.time.format.DateTimeParseException;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class RevisionStatementSupport extends
        AbstractStatementSupport<Revision, RevisionStatement, EffectiveStatement<Revision, RevisionStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.REVISION)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .build();

    public RevisionStatementSupport() {
        super(YangStmtMapping.REVISION);
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