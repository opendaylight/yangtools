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
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.FeatureNamespace;
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

    private FeatureStatementSupport() {
        super(YangStmtMapping.FEATURE);
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
        stmt.addContext(FeatureNamespace.class, stmt.coerceStatementArgument(), stmt);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected FeatureStatement createDeclared(final StmtContext<QName, FeatureStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularFeatureStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected FeatureStatement createEmptyDeclared(@NonNull final StmtContext<QName, FeatureStatement, ?> ctx) {
        return new EmptyFeatureStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected FeatureEffectiveStatement createEffective(
            final StmtContext<QName, FeatureStatement, FeatureEffectiveStatement> ctx, final FeatureStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // TODO Auto-generated method stub
        return new FeatureEffectiveStatementImpl(ctx);
    }

    @Override
    protected FeatureEffectiveStatement createEmptyEffective(
            final StmtContext<QName, FeatureStatement, FeatureEffectiveStatement> ctx,
            final FeatureStatement declared) {
        // TODO Auto-generated method stub
        return new FeatureEffectiveStatementImpl(ctx);
    }
}