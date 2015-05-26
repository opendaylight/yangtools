/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

abstract class AbstractRangeGenerator<T extends Number & Comparable<T>> {
    private static final Map<String, AbstractRangeGenerator<?>> GENERATORS;

    private static void addGenerator(final Builder<String, AbstractRangeGenerator<?>> b, final AbstractRangeGenerator<?> generator) {
        b.put(generator.getClazz().getCanonicalName(), generator);
    }

    static {
        final Builder<String, AbstractRangeGenerator<?>> b = ImmutableMap.<String, AbstractRangeGenerator<?>> builder();
        addGenerator(b, new ByteRangeGenerator());
        addGenerator(b, new ShortRangeGenerator());
        addGenerator(b, new IntegerRangeGenerator());
        addGenerator(b, new LongRangeGenerator());
        addGenerator(b, new BigDecimalRangeGenerator());
        addGenerator(b, new BigIntegerRangeGenerator());
        GENERATORS = b.build();
    }

    private final Class<T> clazz;

    protected AbstractRangeGenerator(final Class<T> clazz) {
        this.clazz = Preconditions.checkNotNull(clazz);
    }

    static AbstractRangeGenerator<?> getInstance(final String canonicalName) {
        return GENERATORS.get(canonicalName);
    }

    protected final Class<T> getClazz() {
        return clazz;
    }

    private String itemType() {
        final StringBuilder sb = new StringBuilder("com.google.common.collect.Range<");
        sb.append(clazz.getCanonicalName()).append('>');

        return sb.toString();
    }

    private String arrayType() {
        return new StringBuilder(itemType()).append("[]").toString();
    }

    protected abstract boolean needsMaximumEnforcement(final T maxToEnforce);
    protected abstract String format(final T number);

    private static StringBuilder appendRangeField(final StringBuilder sb, @Nullable final String member) {
        if (member != null) {
            sb.append('_').append(member);
        }
        return sb.append("_range");
    }

    final String generateRangeDeclaration(@Nullable final String member, @Nonnull final String type) {
        final StringBuilder sb = new StringBuilder();

        sb.append("private static final ").append(arrayType()).append(' ');
        appendRangeField(sb, member).append(";\n");

        return sb.toString();
    }

    final String generateRangeInitializer(@Nullable final String member, @Nonnull final String type, final Collection<RangeConstraint> restrictions) {
        final StringBuilder sb = new StringBuilder("static {\n");

        sb.append("final ").append(arrayType()).append(" a = (").append(arrayType())
        .append(") java.lang.reflect.Array.newInstace(com.google.common.collect.Range.class, ").append(restrictions.size()).append(");\n");

        sb.append("int i = 0;\n");
        for (RangeConstraint r : restrictions) {
            sb.append("a[i++] = com.google.common.collect.Range.closed(")
            .append(format(clazz.cast(r.getMin()))).append(", ").append(format(clazz.cast(r.getMax()))).append(");\n");
        }

        appendRangeField(sb, member).append(" = a;\n");

        return sb.append("\n}").toString();
    }

    final String generateRangeEnforcement(@Nullable final String member, @Nonnull final String type, final String valueReference) {
        final StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("    boolean _valid = false;");
        sb.append("    for (").append(itemType()).append(" r : ");
        appendRangeField(sb, member).append(") {\n");
        sb.append("        if (r.contains(").append(valueReference).append(")) {\n");
        sb.append("            _valid = true;\n");
        sb.append("            break;\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    com.google.common.base.Preconditions.checkArgument(_valid, \"Invalid range: %s, expected %s\", ").append(valueReference).append(", ");
        appendRangeField(sb, member).append(");\n");
        sb.append("}\n");

        return sb.toString();
    }
}
