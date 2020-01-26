/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yang_version;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class YangVersionStatementSupport
        extends BaseStatementSupport<YangVersion, YangVersionStatement, YangVersionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .YANG_VERSION)
        .build();
    private static final YangVersionStatementSupport INSTANCE = new YangVersionStatementSupport();

    private static final @NonNull EmptyYangVersionStatement EMPTY_VER1_DECL =
            new EmptyYangVersionStatement(YangVersion.VERSION_1);
    private static final @NonNull EmptyYangVersionStatement EMPTY_VER1_1_DECL =
            new EmptyYangVersionStatement(YangVersion.VERSION_1_1);
    private static final @NonNull EmptyYangVersionEffectiveStatement EMPTY_VER1_EFF =
            new EmptyYangVersionEffectiveStatement(EMPTY_VER1_DECL);
    private static final @NonNull EmptyYangVersionEffectiveStatement EMPTY_VER1_1_EFF =
            new EmptyYangVersionEffectiveStatement(EMPTY_VER1_1_DECL);

    private YangVersionStatementSupport() {
        super(YangStmtMapping.YANG_VERSION, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static YangVersionStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public YangVersion parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return SourceException.unwrap(YangVersion.parse(value), ctx.getStatementSourceReference(),
            "Unsupported YANG version %s", value);
    }

    @Override
    public void onPreLinkageDeclared(
            final Mutable<YangVersion, YangVersionStatement, YangVersionEffectiveStatement> stmt) {
        stmt.setRootVersion(stmt.getStatementArgument());
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected YangVersionStatement createDeclared(final StmtContext<YangVersion, YangVersionStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularYangVersionStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected YangVersionStatement createEmptyDeclared(final StmtContext<YangVersion, YangVersionStatement, ?> ctx) {
        final YangVersion argument = ctx.coerceStatementArgument();
        switch (argument) {
            case VERSION_1:
                return EMPTY_VER1_DECL;
            case VERSION_1_1:
                return EMPTY_VER1_1_DECL;
            default:
                throw new IllegalStateException("Unhandled version " + argument);
        }
    }

    @Override
    protected YangVersionEffectiveStatement createEffective(
            final StmtContext<YangVersion, YangVersionStatement, YangVersionEffectiveStatement> ctx,
            final YangVersionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularYangVersionEffectiveStatement(declared, substatements);
    }

    @Override
    protected YangVersionEffectiveStatement createEmptyEffective(
            final StmtContext<YangVersion, YangVersionStatement, YangVersionEffectiveStatement> ctx,
            final YangVersionStatement declared) {
        if (EMPTY_VER1_DECL.equals(declared)) {
            return EMPTY_VER1_EFF;
        } else if (EMPTY_VER1_1_DECL.equals(declared)) {
            return EMPTY_VER1_1_EFF;
        } else {
            return new EmptyYangVersionEffectiveStatement(declared);
        }
    }
}
