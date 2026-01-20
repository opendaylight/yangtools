/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.odlext.model.api.InstanceTargetEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.InstanceTargetStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class InstanceTargetStatementSupport
        extends AbstractStringStatementSupport<InstanceTargetStatement, InstanceTargetEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(InstanceTargetStatement.DEFINITION).build();

    public InstanceTargetStatementSupport(final YangParserConfiguration config) {
        super(InstanceTargetStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    protected InstanceTargetStatement createDeclared(final BoundStmtCtx<String> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new InstanceTargetStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected InstanceTargetStatement attachDeclarationReference(final InstanceTargetStatement stmt,
            final DeclarationReference reference) {
        return new RefInstanceTargetStatement(stmt, reference);
    }

    @Override
    protected InstanceTargetEffectiveStatement createEffective(final Current<String, InstanceTargetStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new InstanceTargetEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
