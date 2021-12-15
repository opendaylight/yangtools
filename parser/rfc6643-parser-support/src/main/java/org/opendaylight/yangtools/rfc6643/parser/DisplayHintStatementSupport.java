/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc6643.model.api.DisplayHintEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.DisplayHintStatement;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class DisplayHintStatementSupport
        extends AbstractStringStatementSupport<DisplayHintStatement, DisplayHintEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.DISPLAY_HINT).build();

    public DisplayHintStatementSupport(final YangParserConfiguration config) {
        super(IetfYangSmiv2ExtensionsMapping.DISPLAY_HINT, StatementPolicy.contextIndependent(), config, VALIDATOR);
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