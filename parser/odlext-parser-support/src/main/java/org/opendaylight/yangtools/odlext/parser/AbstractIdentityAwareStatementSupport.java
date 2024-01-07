/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

abstract class AbstractIdentityAwareStatementSupport<D extends DeclaredStatement<QName>,
        E extends EffectiveStatement<QName, D>> extends AbstractStatementSupport<QName, D, E> {
    AbstractIdentityAwareStatementSupport(final StatementDefinition publicDefinition,
            final YangParserConfiguration config, final SubstatementValidator validator) {
        super(publicDefinition, StatementPolicy.exactReplica(), config, validator);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseNodeIdentifier(ctx, value);
    }

    @Override
    public void onStatementDefinitionDeclared(final Mutable<QName, D, E> stmt) {
        final ModelActionBuilder action = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        action.requiresCtx(stmt, ParserNamespaces.IDENTITY, stmt.getArgument(), ModelProcessingPhase.EFFECTIVE_MODEL);

        action.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                // No-op, we just want to ensure the statement is specified
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                throw stmt.newInferenceException("Unable to resolve identity %s", stmt.argument());
            }
        });
    }

    @Override
    protected final E createEffective(final Current<QName, D> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final QName qname = stmt.getArgument();
        final StmtContext<?, ?, IdentityEffectiveStatement> identityCtx =
            verifyNotNull(stmt.namespaceItem(ParserNamespaces.IDENTITY, qname), "Failed to find identity %s", qname);
        return createEffective(stmt.declared(), identityCtx.buildEffective(), substatements);
    }

    abstract @NonNull E createEffective(@NonNull D declared, @NonNull IdentityEffectiveStatement identity,
        ImmutableList<? extends EffectiveStatement<?, ?>> substatements);
}
