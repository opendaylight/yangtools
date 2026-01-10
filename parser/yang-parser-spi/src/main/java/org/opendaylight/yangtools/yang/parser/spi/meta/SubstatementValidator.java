/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class SubstatementValidator {
    private static final StatementCardinality[] EMPTY_MANDATORY = new StatementCardinality[0];

    private final StatementCardinality[] mandatoryStatements;
    private final Map<StatementDefinition, Cardinality> otherStatements;
    private final StatementDefinition currentStatement;

    private SubstatementValidator(final StatementDefinition currentStatement,
            final ImmutableMap<StatementDefinition, Cardinality> cardinalityMap) {
        this.currentStatement = currentStatement;

        // Split the cardinalities based on mandatory/non-mandatory
        mandatoryStatements = cardinalityMap.entrySet().stream()
            .filter(entry -> entry.getValue().minRequired() > 0)
            .map(StatementCardinality::new)
            .toArray(value -> value == 0 ? EMPTY_MANDATORY : new StatementCardinality[value]);
        otherStatements = cardinalityMap.entrySet().stream()
            .filter(entry -> entry.getValue().minRequired() == 0)
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

    public static Builder builder(final StatementDefinition currentStatement) {
        return new Builder(currentStatement);
    }

    public static final class Builder {
        // We are using ImmutableMap because it retains encounter order
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
            return add(def, Cardinality.of(min, max));
        }

        // Equivalent to min .. Integer.MAX_VALUE
        public Builder addAtLeast(final StatementDefinition def, final int min) {
            return add(def, Cardinality.atLeast(min));
        }

        // Equivalent to 0 .. max
        public Builder addAtMost(final StatementDefinition def, final int max) {
            return add(def, Cardinality.atMost(max));
        }

        // Equivalent to 0 .. Integer.MAX_VALUE
        public Builder addAny(final StatementDefinition def) {
            return add(def, Cardinality.any());
        }

        // Equivalent to 1 .. 1
        public Builder addMandatory(final StatementDefinition def) {
            return add(def, Cardinality.exactlyOne());
        }

        // Equivalent to 1 .. MAX
        public Builder addMultiple(final StatementDefinition def) {
            return add(def, Cardinality.atLeastOne());
        }

        // Equivalent to 0 .. 1
        public Builder addOptional(final StatementDefinition def) {
            return add(def, Cardinality.atMostOne());
        }

        public SubstatementValidator build() {
            return new SubstatementValidator(currentStatement, cardinalityMap.build());
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
        // Single pass through all substatements, grouping them by their public defition and computing their frequency.
        // Note we provide an LinkedHashMap::new, so we know the resulting map is mutable and has encounter order
        // matching the document encounter order.
        //
        // We use this as our 'to check' scratchpool, which we then process with the conscious trade-off between the
        // two sets that need to be compared. We often see very little of what is allowed to be present beyond what is
        // required.
        final var stmtCounts = ctx.allSubstatementsStream().collect(
            Collectors.groupingBy(StmtContext::publicDefinition, LinkedHashMap::new, Collectors.summingInt(x -> 1)));

        // The exception to throw. This will be the first offence produced, if there are multiple errors, those will be
        // recorded as having been suppressed by it.
        SourceException toThrow = null;

        // Check if all mandatory statements are present first, removing them from stmtCount, in the order they were
        // defined in the builder. We always have to perform this work.
        for (var entry : mandatoryStatements) {
            final var ex = evaluate(ctx, entry, stmtCounts.remove(entry.def));
            if (ex != null) {
                if (toThrow == null) {
                    toThrow = ex;
                } else {
                    toThrow.addSuppressed(ex);
                }
            }
        }

        // Iterate over other all statements, in the order they were defined in the document. It is not typical to see
        // a model which uses all the allowed substatements, hence this loop ends up using fewer lookups (and no
        // modifications) than what we would get from iterating over otherStaments.
        for (var entry : stmtCounts.entrySet()) {
            final var def = entry.getKey();
            final var ex = evaluate(ctx, def, entry.getValue(), otherStatements.get(def));
            if (ex != null) {
                if (toThrow == null) {
                    toThrow = ex;
                } else {
                    toThrow.addSuppressed(ex);
                }
            }
        }

        // report if there is something to report
        if (toThrow != null) {
            throw toThrow;
        }
    }

    private @Nullable SourceException evaluate(final StmtContext<?, ?, ?> ctx, final StatementCardinality cardinality,
            final @Nullable Integer count) {
        if (count == null) {
            final var root = ctx.getRoot();
            return new MissingSubstatementException(ctx,
                "%s is missing %s. Minimal count is %s. Error in module %s (%s)", currentStatement, cardinality.def,
                cardinality.minRequired, root.rawArgument(),
                ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
        }
        return evaluateMinMax(ctx, cardinality, count);
    }

    private @Nullable SourceException evaluate(final StmtContext<?, ?, ?> ctx, final StatementDefinition def,
            final int count, final @Nullable Cardinality cardinality) {
        if (cardinality == null) {
            if (ctx.namespaceItem(ParserNamespaces.EXTENSION, def.statementName()) == null) {
                final var root = ctx.getRoot();
                return new InvalidSubstatementException(ctx, "%s is not valid for %s. Error in module %s (%s)", def,
                    currentStatement, root.rawArgument(),
                    ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
            }
            return null;
        }
        return evaluateMax(ctx, def, cardinality, count);
    }

    private @Nullable InvalidSubstatementException evaluateMinMax(final StmtContext<?, ?, ?> ctx,
            final StatementCardinality cardinality, final int count) {
        final var def = cardinality.def;
        if (count < cardinality.minRequired()) {
            final var root = ctx.getRoot();
            return new InvalidSubstatementException(ctx,
                "Minimal count of %s for %s is %s, detected %s. Error in module %s (%s)", def, currentStatement,
                cardinality.minRequired(), count, root.rawArgument(),
                ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
        }
        return evaluateMax(ctx, def, cardinality, count);
    }

    private @Nullable InvalidSubstatementException evaluateMax(final StmtContext<?, ?, ?> ctx,
            final StatementDefinition def, final Cardinality cardinality, final int count) {
        if (count > cardinality.maxAllowed()) {
            final var root = ctx.getRoot();
            return new InvalidSubstatementException(ctx,
                "Maximal count of %s for %s is %s, detected %s. Error in module %s (%s)", def, currentStatement,
                cardinality.maxAllowed(), count, root.rawArgument(),
                ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
        }
        return null;
    }

    /**
     * The concept of <a href="https://en.wikipedia.org/wiki/Cardinality">Cardinatlity</a> as applied to
     * {@link Collection#size()}.
     */
    @NonNullByDefault
    public sealed interface Cardinality {
        /**
         * Returns the minimum number of times statement is required to appear.
         *
         * @return the minimum number of times statement is required to appear
         */
        int minRequired();

        /**
         * Returns the maximum number of times statement is allowed to appear.
         *
         * @return the maximum number of times statement is allowed to appear
         */
        int maxAllowed();

        static Cardinality any() {
            return DefaultCardinality.ZERO_MAX;
        }

        static Cardinality exactlyOne() {
            return DefaultCardinality.ONE_ONE;
        }

        static Cardinality atLeast(final int minRequired) {
            return switch (minRequired) {
                case 0 -> DefaultCardinality.ZERO_ZERO;
                case 1 -> DefaultCardinality.ONE_MAX;
                default -> new DefaultCardinality(minRequired, Integer.MAX_VALUE);
            };
        }

        static Cardinality atLeastOne() {
            return DefaultCardinality.ONE_MAX;
        }

        static Cardinality atMost(final int maxAllowed) {
            return switch (maxAllowed) {
                case 0 -> DefaultCardinality.ZERO_ZERO;
                case 1 -> DefaultCardinality.ZERO_ONE;
                case Integer.MAX_VALUE -> DefaultCardinality.ZERO_MAX;
                default -> new DefaultCardinality(0, maxAllowed);
            };
        }

        static Cardinality atMostOne() {
            return DefaultCardinality.ZERO_ONE;
        }

        static Cardinality oneTo(final int maxAllowed) {
            return switch (maxAllowed) {
                case 1 -> DefaultCardinality.ONE_ONE;
                case Integer.MAX_VALUE -> DefaultCardinality.ONE_MAX;
                default -> new DefaultCardinality(1, maxAllowed);
            };
        }

        static Cardinality of(final int minRequired, final int maxAllowed) {
            return switch (minRequired) {
                case 0 -> atMost(maxAllowed);
                case 1 -> oneTo(maxAllowed);
                default -> new DefaultCardinality(minRequired, maxAllowed);
            };
        }
    }

    @NonNullByDefault
    private record DefaultCardinality(int minRequired, int maxAllowed) implements Cardinality {
        static final DefaultCardinality ONE_MAX = new DefaultCardinality(1, Integer.MAX_VALUE);
        static final DefaultCardinality ONE_ONE = new DefaultCardinality(1, 1);
        static final DefaultCardinality ZERO_MAX = new DefaultCardinality(0, Integer.MAX_VALUE);
        static final DefaultCardinality ZERO_ONE = new DefaultCardinality(0, 1);
        static final DefaultCardinality ZERO_ZERO = new DefaultCardinality(0, 0);

        DefaultCardinality {
            checkInvariants(minRequired, maxAllowed);
        }

        static void checkInvariants(final int minRequired, final int maxAllowed) {
            if (minRequired < 0) {
                throw new IllegalArgumentException("Min %s cannot be less than 0!".formatted(minRequired));
            }
            if (maxAllowed < minRequired) {
                throw new IllegalArgumentException(
                    "Min %s can not be greater than max %s!".formatted(minRequired, maxAllowed));
            }
        }
    }

    @NonNullByDefault
    private record StatementCardinality(StatementDefinition def, int minRequired, int maxAllowed)
            implements Cardinality {
        StatementCardinality {
            requireNonNull(def);
            DefaultCardinality.checkInvariants(minRequired, maxAllowed);
        }

        StatementCardinality(final StatementDefinition def, final Cardinality cardinality) {
            this(def, cardinality.minRequired(), cardinality.maxAllowed());
        }

        StatementCardinality(final Entry<StatementDefinition, Cardinality> entry) {
            this(entry.getKey(), entry.getValue());
        }
    }
}
