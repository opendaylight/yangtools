/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc6643.model.api.DisplayHintEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.DisplayHintStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@NonNullByDefault
public final class DisplayHintStatementSupport
        extends AbstractStringStatementSupport<DisplayHintStatement, DisplayHintEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
            SubstatementValidator.builder(DisplayHintStatement.DEFINITION).build();

    public DisplayHintStatementSupport(final YangParserConfiguration config) {
        super(DisplayHintStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    protected DisplayHintStatement createDeclared(final BoundStmtCtx<String> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new DisplayHintStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected DisplayHintStatement attachDeclarationReference(final DisplayHintStatement stmt,
            final DeclarationReference reference) {
        return new RefDisplayHintStatement(stmt, reference);
    }

    @Override
    protected DisplayHintEffectiveStatement createEffective(final Current<String, DisplayHintStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DisplayHintEffectiveStatementImpl(stmt, substatements);
    }
}