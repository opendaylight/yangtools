/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameWithFlagsEffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractNotificationStatementSupport
        extends AbstractSchemaTreeStatementSupport<NotificationStatement, NotificationEffectiveStatement> {
    AbstractNotificationStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.NOTIFICATION, uninstantiatedPolicy(), config, validator);
    }

    @Override
    protected final NotificationStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createNotification(ctx.getArgument(), substatements);
    }

    @Override
    protected final NotificationStatement attachDeclarationReference(final NotificationStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateNotification(stmt, reference);
    }

    @Override
    protected final NotificationEffectiveStatement createEffective(final Current<QName, NotificationStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return EffectiveStatements.createNotification(stmt.declared(), stmt.getArgument(),
                EffectiveStmtUtils.historyAndStatusFlags(stmt.history(), substatements), substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    // FIXME: propagate original?
    public final NotificationEffectiveStatement copyEffective(final Current<QName, NotificationStatement> stmt,
            final NotificationEffectiveStatement original) {
        return EffectiveStatements.copyNotification(original, stmt.getArgument(),
            EffectiveStmtUtils.historyAndStatusFlags(stmt.history(), original.effectiveSubstatements()));
    }

    @Override
    public final EffectiveStatementState extractEffectiveState(final NotificationEffectiveStatement stmt) {
        verify(stmt instanceof NotificationDefinition, "Unexpected statement %s", stmt);
        final var schema = (NotificationDefinition) stmt;
        return new QNameWithFlagsEffectiveStatementState(stmt.argument(),
            EffectiveStmtUtils.historyAndStatusFlags(schema));
    }

    @Override
    public final boolean isIgnoringConfig() {
        return true;
    }
}
