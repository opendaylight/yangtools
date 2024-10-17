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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class SubstatementValidator {
    private final List<Entry<StatementDefinition, Cardinality>> mandatoryStatements;
    private final Map<StatementDefinition, Cardinality> otherStatements;
    private final StatementDefinition currentStatement;

    private SubstatementValidator(final StatementDefinition currentStatement,
            final ImmutableMap<StatementDefinition, Cardinality> cardinalityMap) {
        this.currentStatement = currentStatement;

        // Split the cardinalities based on mandatory/non-mandatory
        mandatoryStatements = cardinalityMap.entrySet().stream()
            .filter(entry -> entry.getValue().min() > 0)
            // Disconnect from source map
            .map(entry -> Map.entry(entry.getKey(), entry.getValue()))
            .collect(Collectors.toUnmodifiableList());
        otherStatements = cardinalityMap.entrySet().stream()
            .filter(entry -> entry.getValue().min() == 0)
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

    public static Builder builder(final StatementDefinition currentStatement) {
        return new Builder(currentStatement);
    }

    public static final class Builder {
        private static final Cardinality ONE_MAX = new Cardinality(1, Integer.MAX_VALUE);
        private static final Cardinality ONE_ONE = new Cardinality(1, 1);
        private static final Cardinality ZERO_MAX = new Cardinality(0, Integer.MAX_VALUE);
        private static final Cardinality ZERO_ONE = new Cardinality(0, 1);

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
        // Note we provide an LinkedHashMap::new, so we know the resulting map is mutable and has a defined encounter
        // order
        final var stmtCounts = ctx.allSubstatementsStream().collect(
            Collectors.groupingBy(StmtContext::publicDefinition, LinkedHashMap::new, Collectors.summingInt(x -> 1)));

        // The exception to throw. This will be the first offence produced, if there are multiple errors, those will be
        // recorded as having been suppressed by it.
        SourceException toThrow = null;

        // Check if all mandatory statements are present first
        for (var entry : mandatoryStatements) {
            final var def = entry.getKey();
            final var ex = evaluate(ctx, def, entry.getValue(), stmtCounts.remove(def));
            if (ex != null) {
                if (toThrow == null) {
                    toThrow = ex;
                } else {
                    toThrow.addSuppressed(ex);
                }
            }
        }

        // Iterate over other all statements
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

        if (toThrow != null) {
            throw toThrow;
        }
    }

    private @Nullable SourceException evaluate(final StmtContext<?, ?, ?> ctx, final StatementDefinition def,
            final Cardinality cardinality, final @Nullable Integer count) {
        if (count == null) {
            final var root = ctx.getRoot();
            return new MissingSubstatementException(ctx,
                "%s is missing %s. Minimal count is %s. Error in module %s (%s)", currentStatement, def,
                cardinality.min(), root.rawArgument(),
                ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
        }
        return evaluateMinMax(ctx, def, cardinality, count);
    }

    private @Nullable SourceException evaluate(final StmtContext<?, ?, ?> ctx, final StatementDefinition def,
            final int count, final @Nullable Cardinality cardinality) {
        if (cardinality == null) {
            if (ctx.namespaceItem(ParserNamespaces.EXTENSION, def.getStatementName()) == null) {
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
            final StatementDefinition def, final Cardinality cardinality, final int count) {
        if (count < cardinality.min()) {
            final var root = ctx.getRoot();
            return new InvalidSubstatementException(ctx,
                "Minimal count of %s for %s is %s, detected %s. Error in module %s (%s)", def, currentStatement,
                cardinality.min(), count, root.rawArgument(),
                ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
        }
        return evaluateMax(ctx, def, cardinality, count);
    }

    private @Nullable InvalidSubstatementException evaluateMax(final StmtContext<?, ?, ?> ctx,
            final StatementDefinition def, final Cardinality cardinality, final int count) {
        if (count > cardinality.max()) {
            final var root = ctx.getRoot();
            return new InvalidSubstatementException(ctx,
                "Maximal count of %s for %s is %s, detected %s. Error in module %s (%s)", def, currentStatement,
                cardinality.max(), count, root.rawArgument(),
                ctx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, root));
        }
        return null;
    }

    private record Cardinality(int min, int max) {
        Cardinality {
            checkArgument(min >= 0, "Min %s cannot be less than 0!", min);
            checkArgument(min <= max, "Min %s can not be greater than max %s!", min, max);
        }
    }
}
