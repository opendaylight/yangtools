/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.when;

import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.PathUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class WhenStatementSupport extends AbstractStatementSupport<RevisionAwareXPath, WhenStatement,
        EffectiveStatement<RevisionAwareXPath, WhenStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.WHEN)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();

    public WhenStatementSupport() {
        super(YangStmtMapping.WHEN);
    }

    @Override
    public RevisionAwareXPath parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return PathUtils.parseXPath(ctx, value);
    }

    @Override
    public WhenStatement createDeclared(final StmtContext<RevisionAwareXPath, WhenStatement, ?> ctx) {
        return new WhenStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<RevisionAwareXPath, WhenStatement> createEffective(
            final StmtContext<RevisionAwareXPath, WhenStatement,
            EffectiveStatement<RevisionAwareXPath, WhenStatement>> ctx) {
        return new WhenEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}