/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import java.text.ParseException;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import java.util.Date;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RevisionDateEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class RevisionDateStatementImpl extends
        AbstractDeclaredStatement<Date> implements RevisionDateStatement {

    protected RevisionDateStatementImpl(
            StmtContext<Date, RevisionDateStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Date, RevisionDateStatement, EffectiveStatement<Date, RevisionDateStatement>> {

        public Definition() {
            super(Rfc6020Mapping.REVISION_DATE);
        }

        @Override
        public Date parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            Date revision;
            try {
                revision = SimpleDateFormatUtil.getRevisionFormat()
                        .parse(value);
            } catch (ParseException e) {
                throw new SourceException(String.format("Revision value %s is not in required format yyyy-MM-dd",
                        value), ctx.getStatementSourceReference(), e);
            }

            return revision;
        }

        @Override
        public RevisionDateStatement createDeclared(
                StmtContext<Date, RevisionDateStatement, ?> ctx) {
            return new RevisionDateStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Date, RevisionDateStatement> createEffective(
                StmtContext<Date, RevisionDateStatement, EffectiveStatement<Date, RevisionDateStatement>> ctx) {
            return new RevisionDateEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public Date getDate() {
        return argument();
    }

}
