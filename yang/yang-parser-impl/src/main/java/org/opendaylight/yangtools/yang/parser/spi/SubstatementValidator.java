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
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;

public final class SubstatementValidator {
    private final Map<StatementDefinition, Cardinality> map;
    private final StatementDefinition currentStatement;
    private final SpecialCase specialCase;
    public final static int MAX = Integer.MAX_VALUE;

    private SubstatementValidator(Builder builder, SpecialCase specialCase) {
        this.map = builder.map.build();
        this.currentStatement = builder.currentStatement;
        this.specialCase = specialCase;
    }

    public static Builder builder(StatementDefinition currentStatement) {
        return new Builder(currentStatement);
    }

    public static class Builder {
        private final ImmutableMap.Builder<StatementDefinition, Cardinality> map = ImmutableMap.builder();
        private final StatementDefinition currentStatement;

        private Builder(StatementDefinition currentStatement) {
            this.currentStatement = currentStatement;
        }

        public Builder add(StatementDefinition d, int min, int max) {
            this.map.put(d, new Cardinality(min, max));
            return this;
        }

        public SubstatementValidator build() {
            return new SubstatementValidator(this, SpecialCase.NULL);
        }

        public SubstatementValidator build(SpecialCase specialCase) {
            return new SubstatementValidator(this, specialCase);
        }
    }

    public void validate(StmtContext ctx) throws YangValidationException{
        final Map<StatementDefinition, Integer> stmtDefMap = new HashMap<>();
        final Map<StatementDefinition, Integer> validatedMap = new HashMap<>();
        final Collection<StatementContextBase<?, ?, ?>> substatementsInit = new ArrayList<>();
        substatementsInit.addAll(ctx.declaredSubstatements());
        substatementsInit.addAll(ctx.effectiveSubstatements());

        // getting and counting all declared and effective substatements
        for (StatementContextBase<?, ?, ?> stmtCtx : substatementsInit) {
            final StatementDefinition definition = stmtCtx.getPublicDefinition();
            if (!stmtDefMap.containsKey(definition)) {
                stmtDefMap.put(definition, 1);
            } else {
                stmtDefMap.put(definition, stmtDefMap.get(definition) + 1);
            }
        }

        if (stmtDefMap.isEmpty() && specialCase == SpecialCase.NOTNULL) {
            throw new YangValidationException(currentStatement + " Must contain atleast 1 element");
        }

        // checking predefined valid statement definition list
        for (Map.Entry entry : stmtDefMap.entrySet()) {
            final StatementDefinition key = (StatementDefinition) entry.getKey();
            if (!map.containsKey(key)) {
                if (ctx.getFromNamespace(ExtensionNamespace.class, key.getArgumentName()) != null) {
                    continue;
                }
                throw new YangValidationException(key + " is not valid for " + currentStatement);
            }
            if (map.get(key).getMin() > (Integer)entry.getValue()) {
                throw new YangValidationException("Minimal count for " + key + " is " + map.get(key).getMin()
                        + ", detected " + entry.getValue());
            }
            if (map.get(key).getMax() < (Integer)entry.getValue()) {
                throw new YangValidationException("Maximal count for " + key + " is " + map.get(key).getMax()
                        + ", detected " + entry.getValue());
            }
            validatedMap.put(key, 1);
        }

        final MapDifference<StatementDefinition, Object> diff = Maps.difference(validatedMap, map);
        final StringBuilder exception = new StringBuilder();

        for (Map.Entry entry : diff.entriesOnlyOnRight().entrySet()) {
            final int min = ((Cardinality)entry.getValue()).getMin();
            if (min > 0) {
                exception.append(currentStatement + " Is missing " + entry.getKey() + ". Minimal " +
                        "count is " + min + "\n");
            }
            if (exception.length() != 0) {
                throw new YangValidationException(exception.toString());
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

    // for cases like refine when there must be at least 1 enum even though all possible elements are set to min 0
    public enum SpecialCase {
        NOTNULL,
        NULL
    }
}