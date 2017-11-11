/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;

import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.PathUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class PathStatementSupport extends AbstractStatementSupport<RevisionAwareXPath, PathStatement,
        EffectiveStatement<RevisionAwareXPath, PathStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .PATH)
        .build();

    public PathStatementSupport() {
        super(YangStmtMapping.PATH);
    }

    @Override
    public RevisionAwareXPath parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return PathUtils.parseXPath(ctx, value);
    }

    @Override
    public PathStatement createDeclared(final StmtContext<RevisionAwareXPath, PathStatement, ?> ctx) {
        return new PathStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<RevisionAwareXPath, PathStatement> createEffective(
            final StmtContext<RevisionAwareXPath, PathStatement,
            EffectiveStatement<RevisionAwareXPath, PathStatement>> ctx) {
        return new PathEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}