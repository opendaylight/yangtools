/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8639.model.api.SubscriptionStateNotificationEffectiveStatement;
import org.opendaylight.yangtools.rfc8639.model.api.SubscriptionStateNotificationStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class SubscriptionStateNotificationStatementSupport
        extends AbstractEmptyStatementSupport<SubscriptionStateNotificationStatement,
            SubscriptionStateNotificationEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(SubscriptionStateNotificationStatement.DEFINITION).build();

    public SubscriptionStateNotificationStatementSupport(final YangParserConfiguration config) {
        super(SubscriptionStateNotificationStatement.DEFINITION, StatementPolicy.exactReplica(), config, VALIDATOR);
    }

    @Override
    public void onStatementAdded(final Mutable<Empty, SubscriptionStateNotificationStatement,
            SubscriptionStateNotificationEffectiveStatement> stmt) {
        if (!stmt.coerceParentContext().producesDeclared(NotificationStatement.class)) {
            throw new SourceException("Only notifications may be marked with subscription-state-notification", stmt);
        }
    }

    @Override
    protected SubscriptionStateNotificationStatement createDeclared(final BoundStmtCtx<Empty> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new SubscriptionStateNotificationStatementImpl(substatements);
    }

    @Override
    protected SubscriptionStateNotificationStatement attachDeclarationReference(
            final SubscriptionStateNotificationStatement stmt, final DeclarationReference reference) {
        return new RefSubscriptionStateNotificationStatement(stmt, reference);
    }

    @Override
    protected SubscriptionStateNotificationEffectiveStatement createEffective(
            final Current<Empty, SubscriptionStateNotificationStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new SubscriptionStateNotificationEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
