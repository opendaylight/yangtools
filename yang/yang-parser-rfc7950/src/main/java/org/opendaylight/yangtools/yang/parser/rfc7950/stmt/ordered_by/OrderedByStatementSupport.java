/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ordered_by;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement.Ordering;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class OrderedByStatementSupport
        extends BaseStatementSupport<Ordering, OrderedByStatement, OrderedByEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.ORDERED_BY).build();
    private static final OrderedByStatementSupport INSTANCE = new OrderedByStatementSupport();

    /*
     * Ordered-by has low argument cardinality, hence we can reuse them in case declaration does not have any
     * substatements (which is the usual case).
     */
    private static final @NonNull EmptyOrderedByStatement EMPTY_SYSTEM_DECL =
            new EmptyOrderedByStatement(Ordering.SYSTEM);
    private static final @NonNull EmptyOrderedByStatement EMPTY_USER_DECL =
            new EmptyOrderedByStatement(Ordering.USER);
    private static final @NonNull EmptyOrderedByEffectiveStatement EMPTY_SYSTEM_EFF =
            new EmptyOrderedByEffectiveStatement(EMPTY_SYSTEM_DECL);
    private static final @NonNull EmptyOrderedByEffectiveStatement EMPTY_USER_EFF =
            new EmptyOrderedByEffectiveStatement(EMPTY_USER_DECL);

    private OrderedByStatementSupport() {
        super(YangStmtMapping.ORDERED_BY);
    }

    public static OrderedByStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Ordering parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Ordering.forArgumentString(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid ordered-by argument '%s'", value);
        }
    }

    @Override
    public String internArgument(final String rawArgument) {
        if ("user".equals(rawArgument)) {
            return "user";
        } else if ("system".equals(rawArgument)) {
            return "system";
        } else {
            return rawArgument;
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected OrderedByStatement createDeclared(final StmtContext<Ordering, OrderedByStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularOrderedByStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected OrderedByStatement createEmptyDeclared(final StmtContext<Ordering, OrderedByStatement, ?> ctx) {
        final Ordering argument = ctx.coerceStatementArgument();
        switch (argument) {
            case SYSTEM:
                return EMPTY_SYSTEM_DECL;
            case USER:
                return EMPTY_USER_DECL;
            default:
                throw new IllegalStateException("Unhandled argument " + argument);
        }
    }

    @Override
    protected OrderedByEffectiveStatement createEffective(
            final StmtContext<Ordering, OrderedByStatement, OrderedByEffectiveStatement> ctx,
            final OrderedByStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularOrderedByEffectiveStatement(declared, substatements);
    }

    @Override
    protected OrderedByEffectiveStatement createEmptyEffective(
            final StmtContext<Ordering, OrderedByStatement, OrderedByEffectiveStatement> ctx,
            final OrderedByStatement declared) {
        // Aggressively reuse effective instances which are backed by the corresponding empty declared instance, as this
        // is the case unless there is a weird extension in use.
        final Ordering argument = declared.getValue();
        switch (argument) {
            case SYSTEM:
                if (EMPTY_SYSTEM_DECL.equals(declared)) {
                    return EMPTY_SYSTEM_EFF;
                }
                break;
            case USER:
                if (EMPTY_USER_DECL.equals(declared)) {
                    return EMPTY_USER_EFF;
                }
                break;
            default:
                throw new IllegalStateException("Unhandled argument " + argument);
        }

        // Declared instance was non-empty, which can happen with extensions
        return new EmptyOrderedByEffectiveStatement(declared);
    }

    @Override
    protected boolean isContextIndependent() {
        return true;
    }
}
