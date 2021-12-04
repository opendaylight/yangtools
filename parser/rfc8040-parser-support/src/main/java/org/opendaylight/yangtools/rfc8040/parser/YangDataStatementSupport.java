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
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class YangDataStatementSupport
        extends AbstractStringStatementSupport<YangDataStatement, YangDataEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR = SubstatementValidator.builder(YangDataStatements.YANG_DATA)
        .addMandatory(YangStmtMapping.CONTAINER)
        .addOptional(YangStmtMapping.USES)
        .build();

    public YangDataStatementSupport(final YangParserConfiguration config) {
        super(YangDataStatements.YANG_DATA, StatementPolicy.reject(), config, VALIDATOR);
    }

    @Override
    public void onStatementAdded(final Mutable<String, YangDataStatement, YangDataEffectiveStatement> ctx) {
        // as per https://tools.ietf.org/html/rfc8040#section-8,
        // yang-data is ignored unless it appears as a top-level statement
        if (ctx.coerceParentContext().getParentContext() != null) {
            ctx.setUnsupported();
        }
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<String, YangDataStatement, YangDataEffectiveStatement> ctx) {
        // Parse and populate our argument to be picked up when we build the effecitve statement
        final String argument = SourceException.throwIfNull(ctx.argument(), ctx, "yang-data requires an argument");
        final QName qname = StmtContextUtils.parseIdentifier(ctx, argument);
        ctx.addToNs(YangDataArgumentNamespace.class, Empty.value(), qname);
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
    protected YangDataStatement createDeclared(@NonNull final StmtContext<String, YangDataStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new YangDataStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected YangDataStatement attachDeclarationReference(final YangDataStatement stmt,
            final DeclarationReference reference) {
        return new RefYangDataStatement(stmt, reference);
    }

    @Override
    protected YangDataEffectiveStatement createEffective(final Current<String, YangDataStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // So now we need to deal with effective validation. The requirement is that:
        //        It MUST contain data definition statements
        //        that result in exactly one container data node definition.
        final long dataDefs = substatements.stream().filter(DataTreeEffectiveStatement.class::isInstance).count();
        if (dataDefs == 0) {
            throw new MissingSubstatementException("yang-data requires exactly one container", stmt.sourceReference());
        }
        if (dataDefs > 1) {
            throw new InvalidSubstatementException(stmt,
                "yang-data requires exactly one data definition node, found %s", dataDefs);
        }

        return new YangDataEffectiveStatementImpl(stmt, substatements,
            verifyNotNull(stmt.namespaceItem(YangDataArgumentNamespace.class, Empty.value())));
    }
}
