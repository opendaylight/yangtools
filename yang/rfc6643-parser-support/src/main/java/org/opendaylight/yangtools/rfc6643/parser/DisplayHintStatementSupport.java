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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class DisplayHintStatementSupport
        extends BaseStringStatementSupport<DisplayHintStatement, DisplayHintEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.DISPLAY_HINT).build();
    private static final DisplayHintStatementSupport INSTANCE = new DisplayHintStatementSupport();

    private DisplayHintStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.DISPLAY_HINT, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static DisplayHintStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected DisplayHintStatement createDeclared(final StmtContext<String, DisplayHintStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new DisplayHintStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected DisplayHintStatement createEmptyDeclared(final StmtContext<String, DisplayHintStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected DisplayHintEffectiveStatement createEffective(final Current<String, DisplayHintStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DisplayHintEffectiveStatementImpl(stmt, substatements);
    }
}