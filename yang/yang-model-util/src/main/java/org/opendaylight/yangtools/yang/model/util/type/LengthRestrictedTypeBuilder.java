/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.ImmutableRangeMap.Builder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.UnresolvedNumber;

public abstract class LengthRestrictedTypeBuilder<T extends LengthRestrictedTypeDefinition<T>>
        extends AbstractRestrictedTypeBuilder<T> {
    private List<LengthConstraint> lengthAlternatives;

    LengthRestrictedTypeBuilder(final T baseType, final SchemaPath path) {
        super(Preconditions.checkNotNull(baseType), path);
    }

    public final void setLengthAlternatives(@Nonnull final Collection<LengthConstraint> lengthAlternatives) {
        Preconditions.checkState(this.lengthAlternatives == null, "Range alternatives already defined as %s",
                lengthAlternatives);
        this.lengthAlternatives = ImmutableList.copyOf(lengthAlternatives);
        touch();
    }

    private static RangeMap<Integer, ConstraintMetaDefinition> ensureResolvedLengths(
            final List<LengthConstraint> unresolved, final RangeMap<Integer, ConstraintMetaDefinition> baseLengths) {
        final Builder<Integer, ConstraintMetaDefinition> builder = ImmutableRangeMap.builder();
        final Range<Integer> span = baseLengths.span();
        for (LengthConstraint c : unresolved) {
            builder.put(Range.closed(resolveLength(c.getMin(), span), resolveLength(c.getMax(), span)), c);
        }

        return builder.build();
    }

    private static Integer resolveLength(final Number unresolved, final Range<Integer> span) {
        if (unresolved instanceof Integer) {
            return (Integer) unresolved;
        }
        if (unresolved instanceof UnresolvedNumber) {
            return ((UnresolvedNumber)unresolved).resolveLength(span);
        }

        return Verify.verifyNotNull(NumberUtil.converterTo(Integer.class)).apply(unresolved);
    }

    private static boolean lengthCovered(final RangeMap<Integer, ConstraintMetaDefinition> where,
            final Range<Integer> what) {
        return where.asMapOfRanges().keySet().stream().anyMatch(range -> range.encloses(what));
    }

    @Override
    final T buildType() {
        final RangeMap<Integer, ConstraintMetaDefinition> baseLengths = findLenghts();

        if (lengthAlternatives == null || lengthAlternatives.isEmpty()) {
            return buildType(baseLengths);
        }

        // Run through alternatives and resolve them against the base type
        final RangeMap<Integer, ConstraintMetaDefinition> resolvedLengths = ensureResolvedLengths(lengthAlternatives,
            baseLengths);

        // Now verify if new ranges are strict subset of base ranges
        for (Entry<Range<Integer>, ConstraintMetaDefinition> entry : resolvedLengths.asMapOfRanges().entrySet()) {
            if (!lengthCovered(baseLengths, entry.getKey())) {
                throw new InvalidLengthConstraintException((LengthConstraint)entry.getValue(),
                        "Length constraint %s is not a subset of parent constraints %s", entry.getValue(), baseLengths);
            }
        }

        return buildType(resolvedLengths);
    }

    abstract T buildType(RangeMap<Integer, ConstraintMetaDefinition> lengthConstraints);

    abstract RangeMap<Integer, ConstraintMetaDefinition> typeLengthConstraints();

    private RangeMap<Integer, ConstraintMetaDefinition> findLenghts() {
        RangeMap<Integer, ConstraintMetaDefinition> ret = ImmutableRangeMap.of();
        T wlk = getBaseType();
        while (wlk != null && ret.asMapOfRanges().isEmpty()) {
            ret = wlk.getLengthConstraints();
            wlk = wlk.getBaseType();
        }

        return ret.asMapOfRanges().isEmpty() ? typeLengthConstraints() : ret;
    }
}
