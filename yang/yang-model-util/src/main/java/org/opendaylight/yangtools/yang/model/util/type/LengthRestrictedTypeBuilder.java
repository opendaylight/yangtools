/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableRangeSet.Builder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;

public abstract class LengthRestrictedTypeBuilder<T extends LengthRestrictedTypeDefinition<T>>
        extends AbstractRestrictedTypeBuilder<T> {
    private LengthConstraint lengthConstraint;

    LengthRestrictedTypeBuilder(final T baseType, final SchemaPath path) {
        super(requireNonNull(baseType), path);
    }

    /**
     * Set a new length constraint.
     *
     * @param constraint Constraint metadata
     * @param ranges Allowed ranges
     * @throws IllegalStateException if the constraint has already been set
     * @throws InvalidLengthConstraintException if one of the proposed ranges does not overlap with supertype
     * @throws NullPointerException if any of the arguments is null
     */
    public final void setLengthConstraint(final @NonNull ConstraintMetaDefinition constraint,
            final @NonNull List<ValueRange> ranges) throws InvalidLengthConstraintException {
        Preconditions.checkState(lengthConstraint == null, "Length constraint already defined as %s", lengthConstraint);
        final LengthConstraint baseLengths = findLenghts();
        if (ranges.isEmpty()) {
            lengthConstraint = baseLengths;
            return;
        }

        // Run through alternatives and resolve them against the base type
        requireNonNull(constraint);
        final Builder<Integer> builder = ImmutableRangeSet.builder();
        final Range<Integer> span = baseLengths.getAllowedRanges().span();

        for (ValueRange c : ranges) {
            builder.add(Range.closed(resolveLength(c.lowerBound(), span), resolveLength(c.upperBound(), span)));
        }

        // Now verify if new ranges are strict subset of base ranges
        final RangeSet<Integer> allowed = builder.build();
        final RangeSet<Integer> baseRanges = baseLengths.getAllowedRanges();
        for (Range<Integer> range : allowed.asRanges()) {
            if (!baseRanges.encloses(range)) {
                throw new InvalidLengthConstraintException("Range %s is not a subset of parent constraint %s", range,
                    baseRanges);
            }
        }

        lengthConstraint = new ResolvedLengthConstraint(constraint, allowed);
        touch();
    }

    abstract T buildType(LengthConstraint lengthConstraint);

    @Override
    final T buildType() {
        return buildType(lengthConstraint != null ? lengthConstraint : findLenghts());
    }

    abstract LengthConstraint typeLengthConstraints();

    private static Integer resolveLength(final Number unresolved, final Range<Integer> span) {
        if (unresolved instanceof Integer) {
            return (Integer) unresolved;
        }
        if (unresolved instanceof UnresolvedNumber) {
            return ((UnresolvedNumber)unresolved).resolveLength(span);
        }

        return Verify.verifyNotNull(NumberUtil.converterTo(Integer.class)).apply(unresolved);
    }

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
