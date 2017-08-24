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
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableRangeSet.Builder;
import com.google.common.collect.Range;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
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

    private static LengthConstraint ensureResolvedLengths(final List<LengthConstraint> unresolved,
            final LengthConstraint baseLengths) {
        final Builder<Comparable<?>> builder = ImmutableRangeSet.builder();
        final Range<Integer> span = baseLengths.getAllowedRanges().span();
        // FIXME: we should not be getting a list of LengthConstraints
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


    @Override
    final T buildType() {
        final LengthConstraint baseLengths = findLenghts();

        if (lengthAlternatives == null || lengthAlternatives.isEmpty()) {
            return buildType(baseLengths);
        }

        // Run through alternatives and resolve them against the base type
        final LengthConstraint resolvedLengths = ensureResolvedLengths(lengthAlternatives, baseLengths);

        // Now verify if new ranges are strict subset of base ranges
        if (!baseLengths.getAllowedRanges().enclosesAll(resolvedLengths.getAllowedRanges())) {
            throw new InvalidLengthConstraintException(resolvedLengths,
                "Length constraint %s is not a subset of parent constraints %s", resolvedLengths, baseLengths);
        }

        return buildType(resolvedLengths);
    }

    abstract T buildType(LengthConstraint lengthConstraint);

    abstract LengthConstraint typeLengthConstraints();

    private LengthConstraint findLenghts() {
        Optional<LengthConstraint> ret = Optional.empty();
        T wlk = getBaseType();
        while (wlk != null && !ret.isPresent()) {
            ret = wlk.getLengthConstraint();
            wlk = wlk.getBaseType();
        }

        return ret.orElse(typeLengthConstraints());
    }
}
