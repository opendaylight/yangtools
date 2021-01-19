/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatement;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatements;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithRawStringArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

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
    }

    private static final YangDataStatementSupport INSTANCE = new YangDataStatementSupport(YangDataStatements.YANG_DATA);

    private final SubstatementValidator validator;

    private YangDataStatementSupport(final StatementDefinition definition) {
        super(definition, StatementPolicy.reject());
        validator = SubstatementValidator.builder(definition)
                .addMandatory(YangStmtMapping.CONTAINER)
                .addOptional(YangStmtMapping.USES)
                .build();
    }

    public static YangDataStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    // FIXME: we could do this in onStatementAdded() instead
    public void onFullDefinitionDeclared(final Mutable<String, YangDataStatement, YangDataEffectiveStatement> ctx) {
        // as per https://tools.ietf.org/html/rfc8040#section-8,
        // yang-data is ignored unless it appears as a top-level statement
        if (ctx.coerceParentContext().getParentContext() != null) {
            ctx.setIsSupportedToBuildEffective(false);
            return;
        }

        // Parse and populate our argument to be picked up when we build the effecitve statement
        final String argument = ctx.argument();
        SourceException.throwIf(argument == null, ctx, "yang-data requires an argument");
        final QName qname = StmtContextUtils.parseIdentifier(ctx, argument);
        ctx.addToNs(YangDataArgumentNamespace.class, Empty.getInstance(), qname);
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
        return new Declared(ctx.getRawArgument(), substatements);
    }

    @Override
    protected YangDataStatement createEmptyDeclared(final StmtContext<String, YangDataStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected YangDataEffectiveStatement createEffective(final Current<String, YangDataStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final QName qname = verifyNotNull(stmt.namespaceItem(YangDataArgumentNamespace.class,
            Empty.getInstance()));

        // in case of yang-data node we need to perform substatement validation at the point when we have
        // effective substatement contexts already available - if the node has only a uses statement declared in it,
        // one top-level container node may very well be added to the yang-data as an effective statement
        validator.validate(stmt.caerbannog());
        return new YangDataEffectiveStatementImpl(stmt, substatements, qname);
    }
}
