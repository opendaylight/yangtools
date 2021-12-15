/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class StatusStatementSupport
        extends AbstractStatementSupport<Status, StatusStatement, StatusEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.STATUS).build();

    public StatusStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.STATUS, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public Status parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        switch (value) {
            case "current":
                return Status.CURRENT;
            case "deprecated":
                return Status.DEPRECATED;
            case "obsolete":
                return Status.OBSOLETE;
            default:
                throw new SourceException(ctx,
                    "Invalid status '%s', must be one of 'current', 'deprecated' or 'obsolete'", value);
        }
    }

    @Override
    public String internArgument(final String rawArgument) {
        if ("current".equals(rawArgument)) {
            return "current";
        } else if ("deprecated".equals(rawArgument)) {
            return "deprecated";
        } else if ("obsolete".equals(rawArgument)) {
            return "obsolete";
        } else {
            return rawArgument;
        }
    }

    @Override
    protected StatusStatement createDeclared(final BoundStmtCtx<Status> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createStatus(ctx.getArgument(), substatements);
    }

    @Override
    protected StatusStatement attachDeclarationReference(final StatusStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateStatus(stmt, reference);
    }

    @Override
    protected StatusEffectiveStatement createEffective(final Current<Status, StatusStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createStatus(stmt.declared(), substatements);
    }
}
