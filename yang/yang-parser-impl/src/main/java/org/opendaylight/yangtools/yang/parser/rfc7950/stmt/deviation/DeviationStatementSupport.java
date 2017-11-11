/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviation;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.PathUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

public final class DeviationStatementSupport extends AbstractStatementSupport<SchemaNodeIdentifier, DeviationStatement,
        EffectiveStatement<SchemaNodeIdentifier, DeviationStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .DEVIATION)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.DEVIATE)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();

    public DeviationStatementSupport() {
        super(YangStmtMapping.DEVIATION);
    }

    @Override
    public SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return PathUtils.nodeIdentifierFromPath(ctx, value);
    }

    @Override
    public DeviationStatement createDeclared(final StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> ctx) {
        return new DeviationStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<SchemaNodeIdentifier, DeviationStatement> createEffective(
            final StmtContext<SchemaNodeIdentifier, DeviationStatement,
            EffectiveStatement<SchemaNodeIdentifier, DeviationStatement>> ctx) {
        return new DeviationEffectiveStatementImpl(ctx);
    }

    @Override
    public void onFullDefinitionDeclared(final StmtContext.Mutable<SchemaNodeIdentifier, DeviationStatement,
            EffectiveStatement<SchemaNodeIdentifier, DeviationStatement>> ctx) {
        final QNameModule currentModule = ctx.getFromNamespace(ModuleCtxToModuleQName.class,
                ctx.getRoot());
        final QNameModule targetModule = ctx.getStatementArgument().getLastComponent().getModule();

        if (currentModule.equals(targetModule)) {
            throw new InferenceException(ctx.getStatementSourceReference(),
                    "Deviation must not target the same module as the one it is defined in: %s", currentModule);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}