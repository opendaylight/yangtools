/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.must;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class MustStatementSupport
        extends BaseStatementSupport<RevisionAwareXPath, MustStatement, MustEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .MUST)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.ERROR_APP_TAG)
        .addOptional(YangStmtMapping.ERROR_MESSAGE)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();
    private static final MustStatementSupport INSTANCE = new MustStatementSupport();

    private MustStatementSupport() {
        super(YangStmtMapping.MUST);
    }

    public static MustStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public RevisionAwareXPath parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.parseXPath(ctx, value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected MustStatement createDeclared(final StmtContext<RevisionAwareXPath, MustStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularMustStatement(ctx, substatements);
    }

    @Override
    protected MustStatement createEmptyDeclared(final StmtContext<RevisionAwareXPath, MustStatement, ?> ctx) {
        return new EmptyMustStatement(ctx);
    }

    @Override
    protected MustEffectiveStatement createEffective(
            final StmtContext<RevisionAwareXPath, MustStatement, MustEffectiveStatement> ctx,
            final MustStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularMustEffectiveStatement(declared, substatements);
    }

    @Override
    protected MustEffectiveStatement createEmptyEffective(
            final StmtContext<RevisionAwareXPath, MustStatement, MustEffectiveStatement> ctx,
            final MustStatement declared) {
        return new EmptyMustEffectiveStatement(declared);
    }
}