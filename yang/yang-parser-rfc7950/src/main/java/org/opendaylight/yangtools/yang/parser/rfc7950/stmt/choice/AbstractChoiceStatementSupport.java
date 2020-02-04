/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImplicitParentAwareStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractChoiceStatementSupport
        extends BaseQNameStatementSupport<ChoiceStatement, ChoiceEffectiveStatement>
        implements ImplicitParentAwareStatementSupport {
    AbstractChoiceStatementSupport() {
        super(YangStmtMapping.CHOICE);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public final Optional<StatementSupport<?, ?, ?>> getImplicitParentFor(final StatementDefinition stmtDef) {
        return YangValidationBundles.SUPPORTED_CASE_SHORTHANDS.contains(stmtDef) ? Optional.of(implictCase())
                : Optional.empty();
    }

    @Override
    public final void onStatementAdded(final Mutable<QName, ChoiceStatement, ChoiceEffectiveStatement> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.coerceStatementArgument(), stmt);
    }

    @Override
    protected final ChoiceStatement createDeclared(@NonNull final StmtContext<QName, ChoiceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularChoiceStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected final ChoiceStatement createEmptyDeclared(@NonNull final StmtContext<QName, ChoiceStatement, ?> ctx) {
        return new EmptyChoiceStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected final ChoiceEffectiveStatement createEffective(
            final StmtContext<QName, ChoiceStatement, ChoiceEffectiveStatement> ctx,
            final ChoiceStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // TODO Auto-generated method stub
        return new ChoiceEffectiveStatementImpl(ctx);
    }

    @Override
    protected final ChoiceEffectiveStatement createEmptyEffective(
            final StmtContext<QName, ChoiceStatement, ChoiceEffectiveStatement> ctx, final ChoiceStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }

    abstract StatementSupport<?, ?, ?> implictCase();
}