/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatements;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithRawStringArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class YangDataStatementSupport
        extends BaseStringStatementSupport<YangDataStatement, YangDataEffectiveStatement> {
    /**
     * Declared statement representation of 'yang-data' extension defined in
     * <a href="https://tools.ietf.org/html/rfc8040#section-8">RFC 8040</a>.
     */
    private static final class Declared extends WithSubstatements implements YangDataStatement {
        Declared(final String rawArgument, final ImmutableList<? extends DeclaredStatement<?>> substatements) {
            super(rawArgument, substatements);
        }

        @Override
        public String getArgument() {
            return argument();
        }
    }

    private static final YangDataStatementSupport INSTANCE = new YangDataStatementSupport(YangDataStatements.YANG_DATA);

    private final SubstatementValidator validator;

    private YangDataStatementSupport(final StatementDefinition definition) {
        super(definition);
        validator = SubstatementValidator.builder(definition)
                .addMandatory(YangStmtMapping.CONTAINER)
                .addOptional(YangStmtMapping.USES)
                .build();
    }

    public static YangDataStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<String, YangDataStatement, YangDataEffectiveStatement> ctx) {
        // as per https://tools.ietf.org/html/rfc8040#section-8,
        // yang-data is ignored unless it appears as a top-level statement
        if (ctx.coerceParentContext().getParentContext() != null) {
            ctx.setIsSupportedToBuildEffective(false);
        }
    }

    @Override
    public boolean isIgnoringIfFeatures() {
        return true;
    }

    @Override
    public boolean isIgnoringConfig() {
        return true;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected YangDataStatement createDeclared(@NonNull final StmtContext<String, YangDataStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new Declared(ctx.coerceRawStatementArgument(), substatements);
    }

    @Override
    protected YangDataStatement createEmptyDeclared(final StmtContext<String, YangDataStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected YangDataEffectiveStatement createEffective(
            final StmtContext<String, YangDataStatement, YangDataEffectiveStatement> ctx,
            final YangDataStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // in case of yang-data node we need to perform substatement validation at the point when we have
        // effective substatement contexts already available - if the node has only a uses statement declared in it,
        // one top-level container node may very well be added to the yang-data as an effective statement
        validator.validate(ctx);
        return new YangDataEffectiveStatementImpl(ctx, substatements);
    }

    @Override
    protected YangDataEffectiveStatement createEmptyEffective(
            final StmtContext<String, YangDataStatement, YangDataEffectiveStatement> ctx,
            final YangDataStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
