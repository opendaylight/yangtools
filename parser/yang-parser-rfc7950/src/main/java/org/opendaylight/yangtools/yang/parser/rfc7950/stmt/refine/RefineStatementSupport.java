/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class RefineStatementSupport
        extends AbstractStatementSupport<Descendant, RefineStatement, RefineEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.REFINE)
        .addOptional(YangStmtMapping.DEFAULT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.CONFIG)
        .addOptional(YangStmtMapping.MANDATORY)
        .addOptional(YangStmtMapping.PRESENCE)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.MIN_ELEMENTS)
        .addOptional(YangStmtMapping.MAX_ELEMENTS)
        .build();
    private static final SubstatementValidator RFC7950_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.REFINE)
        .addOptional(YangStmtMapping.DEFAULT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.CONFIG)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.MANDATORY)
        .addOptional(YangStmtMapping.PRESENCE)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.MIN_ELEMENTS)
        .addOptional(YangStmtMapping.MAX_ELEMENTS)
        .build();

    private RefineStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.REFINE, StatementPolicy.reject(), config, validator);
    }

    public static @NonNull RefineStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new RefineStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull RefineStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new RefineStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public Descendant parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.parseDescendantSchemaNodeIdentifier(ctx, value);
    }

    @Override
    protected RefineStatement createDeclared(final BoundStmtCtx<Descendant> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createRefine(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected RefineStatement attachDeclarationReference(final RefineStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateRefine(stmt, reference);
    }

    @Override
    protected RefineEffectiveStatement createEffective(final Current<Descendant, RefineStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // Empty refine is exceedingly unlikely: let's be lazy and reuse the implementation
        return new RefineEffectiveStatementImpl(stmt.declared(), substatements,
            (SchemaNode) verifyNotNull(stmt.namespaceItem(RefineTargetNamespace.INSTANCE, Empty.value()))
                .buildEffective());
    }
}
