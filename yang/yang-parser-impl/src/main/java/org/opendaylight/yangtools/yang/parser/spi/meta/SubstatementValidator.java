/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

public final class SubstatementValidator {
    /**
     * @deprecated Deprecated since version 1.1.0. Use {@link Builder#addAny(StatementDefinition)},
     *             {@link Builder#addAtLeast(StatementDefinition, int)},
     *             {@link Builder#addMandatory(StatementDefinition)}, or
     *             {@link Builder#addMultiple(StatementDefinition)} instead.
     */
    @Deprecated
    public final static int MAX = Integer.MAX_VALUE;

    private final Map<StatementDefinition, Cardinality> cardinalityMap;
    private final Map<StatementDefinition, Cardinality> mandatoryStatements;
    private final StatementDefinition currentStatement;

    private SubstatementValidator(final Builder builder) {
        this.cardinalityMap = builder.cardinalityMap.build();
        this.currentStatement = builder.currentStatement;
        this.mandatoryStatements = ImmutableMap.copyOf(Maps.filterValues(cardinalityMap, c -> c.getMin() > 0));
    }

    public static Builder builder(final StatementDefinition currentStatement) {
        return new Builder(currentStatement);
    }

    public static class Builder {
        private static final Cardinality ONE_MAX = new Cardinality(1, Integer.MAX_VALUE);
        private static final Cardinality ONE_ONE = new Cardinality(1, 1);
        private static final Cardinality ZERO_MAX = new Cardinality(0, Integer.MAX_VALUE);
        private static final Cardinality ZERO_ONE = new Cardinality(0, 1);

        private final ImmutableMap.Builder<StatementDefinition, Cardinality> cardinalityMap = ImmutableMap.builder();
        private final StatementDefinition currentStatement;

        private Builder(final StatementDefinition currentStatement) {
            this.currentStatement = currentStatement;
        }

        private Builder add(final StatementDefinition d, final Cardinality c) {
            cardinalityMap.put(d, c);
            return this;
        }

        public Builder add(final StatementDefinition d, final int min, final int max) {
            if (max == Integer.MAX_VALUE) {
                return addAtLeast(d, min);
            } else if (min == 0) {
                return addAtMost(d, max);
            } else {
                return add(d, new Cardinality(min, max));
            }
        }

        // Equivalent to min .. Integer.MAX_VALUE
        public Builder addAtLeast(final StatementDefinition d, final int min) {
            switch (min) {
                case 0:
                    return addAny(d);
                case 1:
                    return addMultiple(d);
                default:
                    return add(d, new Cardinality(min, Integer.MAX_VALUE));
            }
        }

        // Equivalent to 0 .. max
        public Builder addAtMost(final StatementDefinition d, final int max) {
            return max == Integer.MAX_VALUE ? addAny(d) : add(d, new Cardinality(0, max));
        }

        // Equivalent to 0 .. Integer.MAX_VALUE
        public Builder addAny(final StatementDefinition d) {
            return add(d, ZERO_MAX);
        }

        // Equivalent to 1 .. 1
        public Builder addMandatory(final StatementDefinition d) {
            return add(d, ONE_ONE);
        }

        // Equivalent to 1 .. MAX
        public Builder addMultiple(final StatementDefinition d) {
            return add(d, ONE_MAX);
        }

        // Equivalent to 0 .. 1
        public Builder addOptional(final StatementDefinition d) {
            return add(d, ZERO_ONE);
        }

        public SubstatementValidator build() {
            return new SubstatementValidator(this);
        }
    }

    public void validate(final StmtContext<?, ?, ?> ctx) throws InvalidSubstatementException,
            MissingSubstatementException {

        final Map<StatementDefinition, Counter> stmtCounts = new HashMap<>();
        for (StmtContext<?, ?, ?> stmtCtx : ctx.allSubstatements()) {
            stmtCounts.computeIfAbsent(stmtCtx.getPublicDefinition(), key -> new Counter()).increment();
        }

        // Mark all mandatory statements as not present. We are using a Map instead of a Set, as it provides us with
        // explicit value in case of failure (which is not important) and a more efficient instantiation performance
        // (which is important).
        final Map<StatementDefinition, Cardinality> missingMandatory = new HashMap<>(mandatoryStatements);

        // Iterate over all statements
        for (Entry<StatementDefinition, Counter> entry : stmtCounts.entrySet()) {
            final StatementDefinition key = entry.getKey();
            final Cardinality cardinality = cardinalityMap.get(key);
            final int value = entry.getValue().getValue();

            if (cardinality == null) {
                if (ctx.getFromNamespace(ExtensionNamespace.class, key.getStatementName()) == null) {
                    throw new InvalidSubstatementException(ctx.getStatementSourceReference(),
                        "%s is not valid for %s. Error in module %s (%s)", key, currentStatement,
                        ctx.getRoot().getStatementArgument(),
                        ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot()));
                }

                continue;
            }

            if (cardinality.getMin() > 0) {
                if (cardinality.getMin() > value) {
                    throw new InvalidSubstatementException(ctx.getStatementSourceReference(),
                        "Minimal count of %s for %s is %s, detected %s. Error in module %s (%s)", key, currentStatement,
                        cardinality.getMin(), value, ctx.getRoot().getStatementArgument(),
                        ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot()));
                }

                // Encountered a mandatory statement, hence we are not missing it
                missingMandatory.remove(key);
            }
            if (cardinality.getMax() < value) {
                throw new InvalidSubstatementException(ctx.getStatementSourceReference(),
                    "Maximal count of %s for %s is %s, detected %s. Error in module %s (%s)", key, currentStatement,
                    cardinality.getMax(), value, ctx.getRoot().getStatementArgument(),
                    ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot()));
            }
        }

        // Check if there are any mandatory statements we have missed
        if (!missingMandatory.isEmpty()) {
            final Entry<StatementDefinition, Cardinality> e = missingMandatory.entrySet().iterator().next();
            final StmtContext<?, ?, ?> root = ctx.getRoot();

            throw new MissingSubstatementException(ctx.getStatementSourceReference(),
                "%s is missing %s. Minimal count is %s. Error in module %s (%s)", currentStatement, e.getKey(),
                e.getValue().getMin(), root.getStatementArgument(), ctx.getFromNamespace(ModuleCtxToModuleQName.class,
                    root));
        }
    }

    private static final class Cardinality {
        private final int min;
        private final int max;

        private Cardinality(final int min, final int max) {
            Preconditions.checkArgument(min >= 0, "Min %s cannot be less than 0!");
            Preconditions.checkArgument(min <= max, "Min %s can not be greater than max %s!", min, max);
            this.min = min;
            this.max = max;
        }

        private int getMax() {
            return max;
        }

        private int getMin() {
            return min;
        }
    }

    private static final class Counter {
        private int value;

        void increment() {
            value++;
        }

        int getValue() {
            return value;
        }
    }
}