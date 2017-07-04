/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PathEffectiveStatementImpl;

public class PathStatementImpl extends AbstractDeclaredStatement<RevisionAwareXPath> implements PathStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .PATH)
            .build();

    protected PathStatementImpl(final StmtContext<RevisionAwareXPath, PathStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<RevisionAwareXPath, PathStatement, EffectiveStatement<RevisionAwareXPath, PathStatement>> {

        public Definition() {
            super(YangStmtMapping.PATH);
        }

        @Override
        public RevisionAwareXPath parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.parseXPath(ctx, value);
        }

        @Override
        public PathStatement createDeclared(final StmtContext<RevisionAwareXPath, PathStatement, ?> ctx) {
            return new PathStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<RevisionAwareXPath, PathStatement> createEffective(
                final StmtContext<RevisionAwareXPath, PathStatement, EffectiveStatement<RevisionAwareXPath, PathStatement>> ctx) {
            return new PathEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getValue() {
        return rawArgument();
    }
}
