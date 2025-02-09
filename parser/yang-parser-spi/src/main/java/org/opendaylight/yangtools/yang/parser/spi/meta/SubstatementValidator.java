/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;

public final class SubstatementValidator {
    private final ImmutableMap<StatementDefinition, Cardinality> cardinalityMap;
    private final ImmutableMap<StatementDefinition, Cardinality> mandatoryStatements;
    private final StatementDefinition currentStatement;

    private SubstatementValidator(final Builder builder) {
        cardinalityMap = builder.cardinalityMap.build();
        currentStatement = builder.currentStatement;
        mandatoryStatements = ImmutableMap.copyOf(Maps.filterValues(cardinalityMap, Cardinality::isMandatory));
    }

    public static Builder builder(final StatementDefinition currentStatement) {
        return new Builder(currentStatement);
    }

    public static final class Builder {
        private static final Cardinality ONE_MAX = new Cardinality(1, Integer.MAX_VALUE);
        private static final Cardinality ONE_ONE = new Cardinality(1, 1);
        private static final Cardinality ZERO_MAX = new Cardinality(0, Integer.MAX_VALUE);
        private static final Cardinality ZERO_ONE = new Cardinality(0, 1);

        private final ImmutableMap.Builder<StatementDefinition, Cardinality> cardinalityMap = ImmutableMap.builder();
        private final StatementDefinition currentStatement;

        Builder(final StatementDefinition currentStatement) {
            this.currentStatement = currentStatement;
        }

        private Builder add(final StatementDefinition def, final Cardinality card) {
            cardinalityMap.put(def, card);
            return this;
        }

        public Builder add(final StatementDefinition def, final int min, final int max) {
            if (max == Integer.MAX_VALUE) {
                return addAtLeast(def, min);
            } else if (min == 0) {
                return addAtMost(def, max);
            } else {
                return add(def, new Cardinality(min, max));
            }
        }

        // Equivalent to min .. Integer.MAX_VALUE
        public Builder addAtLeast(final StatementDefinition def, final int min) {
            return switch (min) {
                case 0 -> addAny(def);
                case 1 -> addMultiple(def);
                default -> add(def, new Cardinality(min, Integer.MAX_VALUE));
            };
        }

        // Equivalent to 0 .. max
        public Builder addAtMost(final StatementDefinition def, final int max) {
            return max == Integer.MAX_VALUE ? addAny(def) : add(def, new Cardinality(0, max));
        }

        // Equivalent to 0 .. Integer.MAX_VALUE
        public Builder addAny(final StatementDefinition def) {
            return add(def, ZERO_MAX);
        }

        // Equivalent to 1 .. 1
        public Builder addMandatory(final StatementDefinition def) {
            return add(def, ONE_ONE);
        }

        // Equivalent to 1 .. MAX
        public Builder addMultiple(final StatementDefinition def) {
            return add(def, ONE_MAX);
        }

        // Equivalent to 0 .. 1
        public Builder addOptional(final StatementDefinition def) {
            return add(def, ZERO_ONE);
        }

        public SubstatementValidator build() {
            return new SubstatementValidator(this);
        }
    }

    /**
     * Validate substatements inside a context.
     *
     * @param ctx Context to inspect
     * @throws InvalidSubstatementException when there is a disallowed statement present.
     * @throws MissingSubstatementException when a mandatory statement is missing.
     */
    public void validate(final StmtContext<?, ?, ?> ctx) {
        final var stmtCounts = ctx.allSubstatementsStream()
            .collect(Collectors.groupingBy(StmtContext::publicDefinition, Collectors.summingInt(x -> 1)));

        // Mark all mandatory statements as not present. We are using a Map instead of a Set, as it provides us with
        // explicit value in case of failure (which is not important) and a more efficient instantiation performance
        // (which is important).
        final var missingMandatory = new HashMap<>(mandatoryStatements);

        // Iterate over all statements
        for (var entry : stmtCounts.entrySet()) {
            final var def = entry.getKey();
            final var cardinality = cardinalityMap.get(def);

            if (cardinality == null) {
                if (ctx.namespaceItem(ParserNamespaces.EXTENSION, def.getStatementName()) == null) {
                    final var root = ctx.getRoot();
                    throw new InvalidSubstatementException(ctx, "%s is not valid for %s. Error in module %s (%s)", def,
                        currentStatement, root.rawArgument(),
                        ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
                }
                continue;
            }

            final int count = entry.getValue();
            if (cardinality.isMandatory()) {
                if (cardinality.min() > count) {
                    final var root = ctx.getRoot();
                    throw new InvalidSubstatementException(ctx,
                        "Minimal count of %s for %s is %s, detected %s. Error in module %s (%s)", def, currentStatement,
                        cardinality.min(), count, root.rawArgument(),
                        ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
                }

                // Encountered a mandatory statement, hence we are not missing it
                missingMandatory.remove(def);
            }
            if (cardinality.max() < count) {
                final var root = ctx.getRoot();
                throw new InvalidSubstatementException(ctx,
                    "Maximal count of %s for %s is %s, detected %s. Error in module %s (%s)", def, currentStatement,
                    cardinality.max(), count, root.rawArgument(),
                    ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
            }
        }

        // Check if there are any mandatory statements we have missed
        if (!missingMandatory.isEmpty()) {
            final var e = missingMandatory.entrySet().iterator().next();
            final var root = ctx.getRoot();

            throw new MissingSubstatementException(ctx,
                "%s is missing %s. Minimal count is %s. Error in module %s (%s)", currentStatement, e.getKey(),
                e.getValue().min(), root.rawArgument(),
                ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
        }
    }

    private record Cardinality(int min, int max) {
        Cardinality {
            checkArgument(min >= 0, "Min %s cannot be less than 0!", min);
            checkArgument(min <= max, "Min %s can not be greater than max %s!", min, max);
        }

        boolean isMandatory() {
            return min > 0;
        }
    }
}
