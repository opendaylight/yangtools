/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.argument;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ArgumentStatementSupport
        extends BaseQNameStatementSupport<ArgumentStatement, ArgumentEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .ARGUMENT)
        .addOptional(YangStmtMapping.YIN_ELEMENT)
        .build();
    private static final ArgumentStatementSupport INSTANCE = new ArgumentStatementSupport();

    private ArgumentStatementSupport() {
        super(YangStmtMapping.ARGUMENT, CopyPolicy.REJECT);
    }

    public static ArgumentStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ArgumentStatement createDeclared(final StmtContext<QName, ArgumentStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularArgumentStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected ArgumentStatement createEmptyDeclared(final StmtContext<QName, ArgumentStatement, ?> ctx) {
        return new EmptyArgumentStatement(ctx.getArgument());
    }

    @Override
    protected ArgumentEffectiveStatement createEffective(final Current<QName, ArgumentStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyArgumentEffectiveStatement(stmt.declared())
            : new RegularArgumentEffectiveStatement(stmt.declared(), substatements);
    }

    @Override
    public @NonNull boolean copyEffective(final ArgumentEffectiveStatement original,
                                          final Current<QName, ArgumentStatement> stmt) {
        return true;
    }
}
