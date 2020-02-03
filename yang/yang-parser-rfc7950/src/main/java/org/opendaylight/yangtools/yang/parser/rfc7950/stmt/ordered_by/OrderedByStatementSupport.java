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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class OrderedByStatementSupport
        extends BaseStringStatementSupport<OrderedByStatement, OrderedByEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.ORDERED_BY).build();
    private static final OrderedByStatementSupport INSTANCE = new OrderedByStatementSupport();

    /*
     * Ordered-by has low argument cardinality, hence we can reuse them in case declaration does not have any
     * substatements (which is the usual case).
     */
    private static final @NonNull EmptyOrderedByStatement EMPTY_SYSTEM_DECL = new EmptyOrderedByStatement("system");
    private static final @NonNull EmptyOrderedByStatement EMPTY_USER_DECL = new EmptyOrderedByStatement("user");
    private static final @NonNull EmptyOrderedByEffectiveStatement EMPTY_SYSTEM_EFF =
            new EmptyOrderedByEffectiveStatement(EMPTY_SYSTEM_DECL);
    private static final @NonNull EmptyOrderedByEffectiveStatement EMPTY_USER_EFF =
            new EmptyOrderedByEffectiveStatement(EMPTY_USER_DECL);

    private OrderedByStatementSupport() {
        super(YangStmtMapping.ORDERED_BY, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static OrderedByStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
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
    protected OrderedByStatement createDeclared(final StmtContext<String, OrderedByStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularOrderedByStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected OrderedByStatement createEmptyDeclared(final StmtContext<String, OrderedByStatement, ?> ctx) {
        final String argument = ctx.coerceStatementArgument();
        switch (argument) {
            case "system":
                return EMPTY_SYSTEM_DECL;
            case "user":
                return EMPTY_USER_DECL;
            default:
                throw new IllegalStateException("Unhandled argument " + argument);
        }
    }

    @Override
    protected OrderedByEffectiveStatement createEffective(
            final StmtContext<String, OrderedByStatement, OrderedByEffectiveStatement> ctx,
            final OrderedByStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularOrderedByEffectiveStatement(declared, substatements);
    }

    @Override
    protected OrderedByEffectiveStatement createEmptyEffective(
            final StmtContext<String, OrderedByStatement, OrderedByEffectiveStatement> ctx,
            final OrderedByStatement declared) {
        // Aggressively reuse effective instances which are backed by the corresponding empty declared instance, as this
        // is the case unless there is a weird extension in use.
        if (EMPTY_USER_DECL.equals(declared)) {
            // Most likely to be seen (as system is the default)
            return EMPTY_USER_EFF;
        } else if (EMPTY_SYSTEM_DECL.equals(declared)) {
            return EMPTY_SYSTEM_EFF;
        } else {
            return new EmptyOrderedByEffectiveStatement(declared);
        }
    }
}
