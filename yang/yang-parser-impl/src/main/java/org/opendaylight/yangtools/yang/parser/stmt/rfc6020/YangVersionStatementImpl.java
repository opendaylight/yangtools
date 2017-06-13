/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.YangVersionEffectiveStatementImpl;

public class YangVersionStatementImpl extends AbstractDeclaredStatement<YangVersion> implements YangVersionStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .YANG_VERSION)
            .build();

    protected YangVersionStatementImpl(final StmtContext<YangVersion, YangVersionStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<YangVersion, YangVersionStatement,
            EffectiveStatement<YangVersion, YangVersionStatement>> {

        public Definition() {
            super(YangStmtMapping.YANG_VERSION);
        }

        @Override
        public YangVersion parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return SourceException.unwrap(YangVersion.parse(value), ctx.getStatementSourceReference(),
                "Unsupported YANG version %s", value);
        }

        @Override
        public YangVersionStatement createDeclared(final StmtContext<YangVersion, YangVersionStatement, ?> ctx) {
            return new YangVersionStatementImpl(ctx);
        }

        @Override
        public void onPreLinkageDeclared(
                final StmtContext.Mutable<YangVersion, YangVersionStatement, EffectiveStatement<YangVersion, YangVersionStatement>> stmt) {
            stmt.setRootVersion(stmt.getStatementArgument());
        }

        @Override
        public EffectiveStatement<YangVersion, YangVersionStatement> createEffective
                (final StmtContext<YangVersion, YangVersionStatement, EffectiveStatement<YangVersion, YangVersionStatement>> ctx) {
            return new YangVersionEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull @Override
    public YangVersion getValue() {
        return argument();
    }
}
