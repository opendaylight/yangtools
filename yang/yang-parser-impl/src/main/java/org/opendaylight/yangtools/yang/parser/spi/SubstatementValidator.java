/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.spi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public final class SubstatementValidator {
    private static final Cardinality ANY = new Cardinality(0, Integer.MAX_VALUE);
    private static final Cardinality ONE = new Cardinality(1, 1);
    private static final Cardinality ZERO_ONE = new Cardinality(0, 1);

    private final Map<StatementDefinition, Cardinality> cardinalityMap;
    private final StatementDefinition currentStatement;
    private final SpecialCase specialCase;

    private SubstatementValidator(final Builder builder, final SpecialCase specialCase) {
        this.cardinalityMap = builder.cardinalityMap.build();
        this.currentStatement = builder.currentStatement;
        this.specialCase = specialCase;
    }

    public static Builder builder(final StatementDefinition currentStatement) {
        return new Builder(currentStatement);
    }

    public static class Builder {
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
            return max == Integer.MAX_VALUE ? addAtLeast(d, min) : add(d, new Cardinality(min, max));
        }

        // Equivalent to min .. Integer.MAX_VALUE
        public Builder addAtLeast(final StatementDefinition d, final int min) {
            if (min == 0) {
                return addAny(d);
            }

            return add(d, new Cardinality(min, Integer.MAX_VALUE));
        }

        // Equivalent to 0 .. Integer.MAX_VALUE
        public Builder addAny(final StatementDefinition d) {
            return add(d, ANY);
        }

        // Equivalent to 1 .. 1
        public Builder addMandatory(final StatementDefinition d) {
            return add(d, ONE);
        }

        // Equivalent to 0 .. 1
        public Builder addOptional(final StatementDefinition d) {
            return add(d, ZERO_ONE);
        }

        public SubstatementValidator build() {
            return new SubstatementValidator(this, SpecialCase.NULL);
        }

        public SubstatementValidator build(final SpecialCase specialCase) {
            return new SubstatementValidator(this, specialCase);
        }
    }

    public void validate(final StmtContext<?, ?, ?> ctx) throws InvalidSubstatementException,
            MissingSubstatementException {
        final Iterable<StatementContextBase<?, ?, ?>> substatementsInit = Iterables.concat(
            ctx.declaredSubstatements(), ctx.effectiveSubstatements());

        final Map<StatementDefinition, Integer> stmtDefMap = new HashMap<>();
        for (StatementContextBase<?, ?, ?> stmtCtx : substatementsInit) {
            final StatementDefinition definition = stmtCtx.getPublicDefinition();
            if (!stmtDefMap.containsKey(definition)) {
                stmtDefMap.put(definition, 1);
            } else {
                stmtDefMap.put(definition, stmtDefMap.get(definition) + 1);
            }
        }

        if (stmtDefMap.isEmpty() && specialCase == SpecialCase.NOTNULL) {
            throw new InvalidSubstatementException(ctx.getStatementSourceReference(),
                "%s must contain atleast 1 element. Error in module %s (%s)", currentStatement,
                ctx.getRoot().getStatementArgument(),
                ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot()));
        }

        final Map<StatementDefinition, Integer> validatedMap = new HashMap<>();
        for (Entry<?, ?> entry : stmtDefMap.entrySet()) {
            final StatementDefinition key = (StatementDefinition) entry.getKey();
            if (!cardinalityMap.containsKey(key)) {
                if (ctx.getFromNamespace(ExtensionNamespace.class, key.getStatementName()) != null) {
                    continue;
                }
                throw new InvalidSubstatementException(ctx.getStatementSourceReference(),
                    "%s is not valid for %s. Error in module %s (%s)", key, currentStatement,
                    ctx.getRoot().getStatementArgument(),
                    ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot()));
            }
            if (cardinalityMap.get(key).getMin() > (Integer) entry.getValue()) {
                throw new InvalidSubstatementException(ctx.getStatementSourceReference(),
                    "Minimal count of %s for %s is %s, detected %s. Error in module %s (%s)", key, currentStatement,
                    cardinalityMap.get(key).getMin(), entry.getValue(), ctx.getRoot().getStatementArgument(),
                    ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot()));
            }
            if (cardinalityMap.get(key).getMax() < (Integer) entry.getValue()) {
                throw new InvalidSubstatementException(ctx.getStatementSourceReference(),
                    "Maximal count of %s for %s is %s, detected %s. Error in module %s (%s)", key, currentStatement,
                    cardinalityMap.get(key).getMax(), entry.getValue(), ctx.getRoot().getStatementArgument(),
                    ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot()));
            }
            validatedMap.put(key, 1);
        }

        final MapDifference<StatementDefinition, Object> diff = Maps.difference(validatedMap, cardinalityMap);
        for (Entry<?, ?> entry : diff.entriesOnlyOnRight().entrySet()) {
            final int min = ((Cardinality) entry.getValue()).getMin();
            if (min > 0) {
                throw new MissingSubstatementException(ctx.getStatementSourceReference(),
                    "%s is missing %s. Minimal count is %s. Error in module %s (%s)", currentStatement, entry.getKey(),
                    min, ctx.getRoot().getStatementArgument(),
                    ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot()));
            }
        }
    }

    private static class Cardinality {
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

    public enum SpecialCase {
        NOTNULL,
        NULL
    }
}