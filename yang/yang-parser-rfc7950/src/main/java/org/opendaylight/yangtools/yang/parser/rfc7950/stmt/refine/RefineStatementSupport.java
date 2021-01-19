/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class RefineStatementSupport
        extends AbstractStatementSupport<Descendant, RefineStatement, RefineEffectiveStatement> {
    private static final @NonNull RefineStatementSupport RFC6020_INSTANCE = new RefineStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.REFINE)
            .addOptional(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.CONFIG)
            .addOptional(YangStmtMapping.MANDATORY)
            .addOptional(YangStmtMapping.PRESENCE)
            .addAny(YangStmtMapping.MUST)
            .addOptional(YangStmtMapping.MIN_ELEMENTS)
            .addOptional(YangStmtMapping.MAX_ELEMENTS)
            .build());
    private static final @NonNull RefineStatementSupport RFC7950_INSTANCE = new RefineStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.REFINE)
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
            .build());

    private final SubstatementValidator validator;

    private RefineStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.REFINE, CopyPolicy.DECLARED_COPY);
        this.validator = requireNonNull(validator);
    }

    public static @NonNull RefineStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull RefineStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    public Descendant parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.parseDescendantSchemaNodeIdentifier(ctx, value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected RefineStatement createDeclared(final StmtContext<Descendant, RefineStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RefineStatementImpl(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected RefineStatement createEmptyDeclared(final StmtContext<Descendant, RefineStatement, ?> ctx) {
        // Empty refine is exceedingly unlikely: let's be lazy and reuse the implementation
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected RefineEffectiveStatement createEffective(final Current<Descendant, RefineStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // Empty refine is exceedingly unlikely: let's be lazy and reuse the implementation
        return new RefineEffectiveStatementImpl(stmt.declared(), substatements, stmt.wrapSchemaPath(),
            (SchemaNode) verifyNotNull(stmt.namespaceItem(RefineTargetNamespace.class, Empty.getInstance()))
                .buildEffective());
    }
}
