/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;

import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class PathStatementSupport extends AbstractStatementSupport<PathExpression, PathStatement,
        EffectiveStatement<PathExpression, PathStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .PATH)
        .build();
    private static final PathStatementSupport INSTANCE = new PathStatementSupport();

    private PathStatementSupport() {
        super(YangStmtMapping.PATH);
    }

    public static PathStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public PathExpression parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return AntlrPathExpression.parse(ctx, value);
    }

    @Override
    public PathStatement createDeclared(final StmtContext<PathExpression, PathStatement, ?> ctx) {
        return new PathStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<PathExpression, PathStatement> createEffective(
            final StmtContext<PathExpression, PathStatement, EffectiveStatement<PathExpression, PathStatement>> ctx) {
        return new PathEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}