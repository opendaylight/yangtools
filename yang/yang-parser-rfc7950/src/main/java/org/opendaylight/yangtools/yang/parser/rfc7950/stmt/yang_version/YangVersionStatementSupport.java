/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yang_version;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class YangVersionStatementSupport
        extends AbstractStatementSupport<YangVersion, YangVersionStatement, YangVersionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.YANG_VERSION).build();
    private static final YangVersionStatementSupport INSTANCE = new YangVersionStatementSupport();

    private YangVersionStatementSupport() {
        super(YangStmtMapping.YANG_VERSION, StatementPolicy.reject());
    }

    public static YangVersionStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public YangVersion parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return SourceException.unwrap(YangVersion.parse(value), ctx,  "Unsupported YANG version %s", value);
    }

    @Override
    public void onPreLinkageDeclared(
            final Mutable<YangVersion, YangVersionStatement, YangVersionEffectiveStatement> stmt) {
        stmt.setRootVersion(stmt.argument());
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected YangVersionStatement createDeclared(final StmtContext<YangVersion, YangVersionStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createYangVersion(ctx.getArgument(), substatements);
    }

    @Override
    protected YangVersionStatement createEmptyDeclared(final StmtContext<YangVersion, YangVersionStatement, ?> ctx) {
        return DeclaredStatements.createYangVersion(ctx.getArgument());
    }

    @Override
    protected YangVersionEffectiveStatement createEffective(final Current<YangVersion, YangVersionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createYangVersion(stmt.declared(), substatements);
    }
}
