/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.status;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class StatusStatementSupport
        extends BaseStatementSupport<Status, StatusStatement, StatusEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .STATUS)
        .build();
    private static final StatusStatementSupport INSTANCE = new StatusStatementSupport();

    /*
     * status has low argument cardinality, hence we can reuse them in case declaration does not have any
     * substatements (which is the usual case). Yeah, we could consider an EnumMap, but this is not too bad, either.
     */
    private static final @NonNull EmptyStatusStatement EMPTY_CURRENT_DECL =
            new EmptyStatusStatement(Status.CURRENT);
    private static final @NonNull EmptyStatusStatement EMPTY_DEPRECATED_DECL =
            new EmptyStatusStatement(Status.DEPRECATED);
    private static final @NonNull EmptyStatusStatement EMPTY_OBSOLETE_DECL =
            new EmptyStatusStatement(Status.OBSOLETE);
    private static final @NonNull EmptyStatusEffectiveStatement EMPTY_CURRENT_EFF =
            new EmptyStatusEffectiveStatement(EMPTY_CURRENT_DECL);
    private static final @NonNull EmptyStatusEffectiveStatement EMPTY_DEPRECATED_EFF =
            new EmptyStatusEffectiveStatement(EMPTY_DEPRECATED_DECL);
    private static final @NonNull EmptyStatusEffectiveStatement EMPTY_OBSOLETE_EFF =
            new EmptyStatusEffectiveStatement(EMPTY_OBSOLETE_DECL);

    private StatusStatementSupport() {
        super(YangStmtMapping.STATUS, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static StatusStatementSupport getInstance() {
        return INSTANCE;
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
                throw new SourceException(ctx.getStatementSourceReference(),
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
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected StatusStatement createDeclared(final StmtContext<Status, StatusStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularStatusStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected StatusStatement createEmptyDeclared(final StmtContext<Status, StatusStatement, ?> ctx) {
        final Status argument = ctx.coerceStatementArgument();
        switch (argument) {
            case CURRENT:
                return EMPTY_CURRENT_DECL;
            case DEPRECATED:
                return EMPTY_DEPRECATED_DECL;
            case OBSOLETE:
                return EMPTY_OBSOLETE_DECL;
            default:
                throw new IllegalStateException("Unhandled argument " + argument);
        }
    }

    @Override
    protected StatusEffectiveStatement createEffective(
            final StmtContext<Status, StatusStatement, StatusEffectiveStatement> ctx, final StatusStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularStatusEffectiveStatement(declared, substatements);
    }

    @Override
    protected StatusEffectiveStatement createEmptyEffective(
            final StmtContext<Status, StatusStatement, StatusEffectiveStatement> ctx, final StatusStatement declared) {
        // Aggressively reuse effective instances which are backed by the corresponding empty declared instance, as this
        // is the case unless there is a weird extension in use.
        if (EMPTY_DEPRECATED_DECL.equals(declared)) {
            // Most likely to be seen (as current is the default)
            return EMPTY_DEPRECATED_EFF;
        } else if (EMPTY_OBSOLETE_DECL.equals(declared)) {
            // less likely
            return EMPTY_OBSOLETE_EFF;
        } else if (EMPTY_CURRENT_DECL.equals(declared)) {
            // ... okay, why is this there? :)
            return EMPTY_CURRENT_EFF;
        } else {
            return new EmptyStatusEffectiveStatement(declared);
        }
    }
}
