/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.YangVersionEffectiveStatementImpl;

public class YangVersionStatementImpl extends AbstractDeclaredStatement<String> implements YangVersionStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .YANG_VERSION)
            .build();

    protected YangVersionStatementImpl(final StmtContext<String, YangVersionStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,YangVersionStatement,
            EffectiveStatement<String,YangVersionStatement>> {

        public Definition() {
            super(Rfc6020Mapping.YANG_VERSION);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public YangVersionStatement createDeclared(final StmtContext<String, YangVersionStatement, ?> ctx) {
            return new YangVersionStatementImpl(ctx);
        }

        @Override
        public void onPreLinkageDeclared(
                final StmtContext.Mutable<String, YangVersionStatement, EffectiveStatement<String, YangVersionStatement>> stmt) {
            stmt.setRootVersion(SemVer.valueOf(stmt.getStatementArgument()));
        };

        @Override
        public EffectiveStatement<String, YangVersionStatement> createEffective
                (final StmtContext<String, YangVersionStatement, EffectiveStatement<String, YangVersionStatement>> ctx) {
            return new YangVersionEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<String, YangVersionStatement,
                EffectiveStatement<String, YangVersionStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull @Override
    public String getValue() {
        return rawArgument();
    }
}
