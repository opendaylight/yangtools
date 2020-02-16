/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

public final class DeviationStatementSupport
        extends BaseStatementSupport<SchemaNodeIdentifier, DeviationStatement, DeviationEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .DEVIATION)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.DEVIATE)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();
    private static final DeviationStatementSupport INSTANCE = new DeviationStatementSupport();

    private DeviationStatementSupport() {
        super(YangStmtMapping.DEVIATION);
    }

    public static DeviationStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.nodeIdentifierFromPath(ctx, value);
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<SchemaNodeIdentifier, DeviationStatement, DeviationEffectiveStatement> ctx) {
        final QNameModule currentModule = ctx.getFromNamespace(ModuleCtxToModuleQName.class,
                ctx.getRoot());
        final QNameModule targetModule = Iterables.getLast(ctx.coerceStatementArgument().getNodeIdentifiers())
                .getModule();

        if (currentModule.equals(targetModule)) {
            throw new InferenceException(ctx.getStatementSourceReference(),
                    "Deviation must not target the same module as the one it is defined in: %s", currentModule);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected DeviationStatement createDeclared(final StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new DeviationStatementImpl(ctx, substatements);
    }

    @Override
    protected DeviationStatement createEmptyDeclared(
            final StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> ctx) {
        return new DeviationStatementImpl(ctx, ImmutableList.of());
    }

    @Override
    protected DeviationEffectiveStatement createEffective(
            final StmtContext<SchemaNodeIdentifier, DeviationStatement, DeviationEffectiveStatement> ctx,
            final DeviationStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DeviationEffectiveStatementImpl(declared, substatements);
    }

    @Override
    protected DeviationEffectiveStatement createEmptyEffective(
            final StmtContext<SchemaNodeIdentifier, DeviationStatement, DeviationEffectiveStatement> ctx,
            final DeviationStatement declared) {
        return new DeviationEffectiveStatementImpl(declared, ImmutableList.of());
    }
}