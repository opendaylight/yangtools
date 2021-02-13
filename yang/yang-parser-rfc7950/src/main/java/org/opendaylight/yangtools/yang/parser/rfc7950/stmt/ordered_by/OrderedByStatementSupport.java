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
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.spi.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class OrderedByStatementSupport
        extends AbstractStatementSupport<Ordering, OrderedByStatement, OrderedByEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.ORDERED_BY).build();
    private static final OrderedByStatementSupport INSTANCE = new OrderedByStatementSupport();

    /*
     * Ordered-by has low argument cardinality, hence we can reuse them in case declaration does not have any
     * substatements (which is the usual case).
     */
    // FIXME: move this to yang-model-spi
    private static final @NonNull OrderedByStatement EMPTY_SYSTEM_DECL =
        DeclaredStatements.createOrderedBy(Ordering.SYSTEM);
    private static final @NonNull OrderedByStatement EMPTY_USER_DECL =
        DeclaredStatements.createOrderedBy(Ordering.USER);
    private static final @NonNull OrderedByEffectiveStatement EMPTY_SYSTEM_EFF =
        EffectiveStatements.createOrderedBy(EMPTY_SYSTEM_DECL);
    private static final @NonNull OrderedByEffectiveStatement EMPTY_USER_EFF =
        EffectiveStatements.createOrderedBy(EMPTY_USER_DECL);

    private OrderedByStatementSupport() {
        super(YangStmtMapping.ORDERED_BY, StatementPolicy.contextIndependent());
    }

    public static OrderedByStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Ordering parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Ordering.forArgument(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid ordered-by argument '%s'", value);
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
        return DeclaredStatements.createOrderedBy(ctx.getArgument(), substatements);
    }

    @Override
    protected OrderedByStatement createEmptyDeclared(final StmtContext<Ordering, OrderedByStatement, ?> ctx) {
        final Ordering argument = ctx.getArgument();
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
    protected OrderedByEffectiveStatement createEffective(final Current<Ordering, OrderedByStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createEmptyEffective(stmt.declared())
            : EffectiveStatements.createOrderedBy(stmt.declared(), substatements);
    }

    private static @NonNull OrderedByEffectiveStatement createEmptyEffective(final OrderedByStatement declared) {
        // Aggressively reuse effective instances which are backed by the corresponding empty declared instance, as this
        // is the case unless there is a weird extension in use.
        if (EMPTY_USER_DECL.equals(declared)) {
            // Most likely to be seen (as system is the default)
            return EMPTY_USER_EFF;
        } else if (EMPTY_SYSTEM_DECL.equals(declared)) {
            return EMPTY_SYSTEM_EFF;
        } else {
            return EffectiveStatements.createOrderedBy(declared);
        }
    }
}
