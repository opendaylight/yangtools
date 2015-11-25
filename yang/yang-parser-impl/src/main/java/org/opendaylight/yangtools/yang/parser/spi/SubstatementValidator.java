/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.spi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class SubstatementValidator {
    private final Map<StatementDefinition, Cardinality> cardinalityMap;
    private final StatementDefinition currentStatement;
    private final SpecialCase specialCase;
    public final static int MAX = Integer.MAX_VALUE;

    private SubstatementValidator(Builder builder, SpecialCase specialCase) {
        this.cardinalityMap = builder.cardinalityMap.build();
        this.currentStatement = builder.currentStatement;
        this.specialCase = specialCase;
    }

    public static Builder builder(StatementDefinition currentStatement) {
        return new Builder(currentStatement);
    }

    public static class Builder {
        private final ImmutableMap.Builder<StatementDefinition, Cardinality> cardinalityMap = ImmutableMap.builder();
        private final StatementDefinition currentStatement;

        private Builder(StatementDefinition currentStatement) {
            this.currentStatement = currentStatement;
        }

        public Builder add(StatementDefinition d, int min, int max) {
            this.cardinalityMap.put(d, new Cardinality(min, max));
            return this;
        }

        public SubstatementValidator build() {
            return new SubstatementValidator(this, SpecialCase.NULL);
        }

        public SubstatementValidator build(SpecialCase specialCase) {
            return new SubstatementValidator(this, specialCase);
        }
    }

    public void validate(StmtContext ctx) throws InvalidSubstatementException, MissingSubstatementException{
        final Map<StatementDefinition, Integer> stmtDefMap = new HashMap<>();
        final Map<StatementDefinition, Integer> validatedMap = new HashMap<>();
        final Collection<StatementContextBase<?, ?, ?>> substatementsInit = new ArrayList<>();
        substatementsInit.addAll(ctx.declaredSubstatements());
        substatementsInit.addAll(ctx.effectiveSubstatements());

        for (StatementContextBase<?, ?, ?> stmtCtx : substatementsInit) {
            final StatementDefinition definition = stmtCtx.getPublicDefinition();
            if (!stmtDefMap.containsKey(definition)) {
                stmtDefMap.put(definition, 1);
            } else {
                stmtDefMap.put(definition, stmtDefMap.get(definition) + 1);
            }
        }

        if (stmtDefMap.isEmpty() && specialCase == SpecialCase.NOTNULL) {
            throw new InvalidSubstatementException(String.format("%s must contain atleast 1 element. Error in module " +
                            "%s (%s)", currentStatement, ctx.getRoot().getStatementArgument(), ctx.getFromNamespace
                            (ModuleCtxToModuleQName.class, ctx.getRoot())), ctx.getStatementSourceReference());
        }

        for (Map.Entry entry : stmtDefMap.entrySet()) {
            final StatementDefinition key = (StatementDefinition) entry.getKey();
            if (!cardinalityMap.containsKey(key)) {
                if (ctx.getFromNamespace(ExtensionNamespace.class, key.getArgumentName()) != null) {
                    continue;
                }
                throw new InvalidSubstatementException(String.format("%s is not valid for %s. Error in module %s (%s)",
                        key, currentStatement, ctx.getRoot().getStatementArgument(),
                        ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot())),
                        ctx.getStatementSourceReference());
            }
            if (cardinalityMap.get(key).getMin() > (Integer)entry.getValue()) {
                throw new InvalidSubstatementException(String.format("Minimal count of %s for %s is %s, detected %s. " +
                                "Error in module %s (%s)", key, currentStatement, cardinalityMap.get(key).getMin(),
                        entry.getValue(), ctx.getRoot().getStatementArgument(),
                        ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot())),
                        ctx.getStatementSourceReference());
            }
            if (cardinalityMap.get(key).getMax() < (Integer)entry.getValue()) {
                throw new InvalidSubstatementException(String.format("Maximal count of %s for %s is %s, detected %s. " +
                                "Error in module %s (%s)", key, currentStatement, cardinalityMap.get(key).getMax(),
                        entry.getValue(), ctx.getRoot().getStatementArgument(),
                        ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot())),
                        ctx.getStatementSourceReference());
            }
            validatedMap.put(key, 1);
        }

        final MapDifference<StatementDefinition, Object> diff = Maps.difference(validatedMap, cardinalityMap);

        for (Map.Entry entry : diff.entriesOnlyOnRight().entrySet()) {
            final int min = ((Cardinality)entry.getValue()).getMin();
            if (min > 0) {
                throw new MissingSubstatementException(String.format("%s is missing %s. Minimal count is %s. Error in" +
                        "  module %s (%s)", currentStatement, entry.getKey(), min, ctx.getRoot().getStatementArgument(),
                        ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx.getRoot())),
                        ctx.getStatementSourceReference());
            }
        }
    }

    private static class Cardinality {
        private final int min;
        private final int max;

        private Cardinality(int min, int max) throws YangParseException {
            if (min > max) {
                throw new IllegalArgumentException("Min can not be greater than max!");
            }
            if (min < 0) {
                throw new IllegalArgumentException("Min can not be les than 0!");
            }
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