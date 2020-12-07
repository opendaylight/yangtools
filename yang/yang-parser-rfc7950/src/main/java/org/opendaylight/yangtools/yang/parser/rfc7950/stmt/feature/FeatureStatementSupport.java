/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.feature;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.FeatureNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class FeatureStatementSupport
        extends BaseQNameStatementSupport<FeatureStatement, FeatureEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.FEATURE)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();
    private static final FeatureStatementSupport INSTANCE = new FeatureStatementSupport();
    private static final int EMPTY_EFFECTIVE_FLAGS = new FlagsBuilder().setStatus(Status.CURRENT).toFlags();

    private FeatureStatementSupport() {
        super(YangStmtMapping.FEATURE, CopyPolicy.REJECT);
    }

    public static FeatureStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, FeatureStatement, FeatureEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);
        stmt.addContext(FeatureNamespace.class, stmt.getArgument(), stmt);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected FeatureStatement createDeclared(final StmtContext<QName, FeatureStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularFeatureStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected FeatureStatement createEmptyDeclared(@NonNull final StmtContext<QName, FeatureStatement, ?> ctx) {
        return new EmptyFeatureStatement(ctx.getArgument());
    }

    @Override
    protected FeatureEffectiveStatement createEffective(final Current<QName, FeatureStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty()
            ? new EmptyFeatureEffectiveStatement(stmt.declared(), stmt.wrapSchemaPath(), EMPTY_EFFECTIVE_FLAGS)
                : new RegularFeatureEffectiveStatement(stmt.declared(), stmt.wrapSchemaPath(),
                    computeFlags(substatements), substatements);
    }

    @Override
    public @NonNull boolean copyEffective(final FeatureEffectiveStatement original,
                                          final Current<QName, FeatureStatement> stmt) {
        return ((EmptyFeatureEffectiveStatement) original).getPath().equals(stmt.wrapSchemaPath());
    }

    private static int computeFlags(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();
    }
}