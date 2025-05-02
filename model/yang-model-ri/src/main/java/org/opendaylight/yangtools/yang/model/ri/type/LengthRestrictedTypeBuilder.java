/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRanges;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;

public abstract class LengthRestrictedTypeBuilder<T extends LengthRestrictedTypeDefinition<T>>
        extends AbstractRestrictedTypeBuilder<T> {
    private LengthConstraint lengthConstraint;

    LengthRestrictedTypeBuilder(final T baseType, final QName qname) {
        super(requireNonNull(baseType), qname);
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
            final @NonNull ValueRanges ranges) throws InvalidLengthConstraintException {
        if (lengthConstraint != null) {
            throw new IllegalStateException("Length constraint already defined as " + lengthConstraint);
        }

        // Run through alternatives and resolve them against the base type
        final var baseLengths = findLenghts();
        requireNonNull(constraint);
        final var builder = ImmutableRangeSet.<Integer>builder();
        final var span = baseLengths.getAllowedRanges().span();
        for (var range : ranges) {
            builder.add(Range.closed(resolveLength(range.lowerBound(), span), resolveLength(range.upperBound(), span)));
        }

        // Now verify if new ranges are strict subset of base ranges
        final var allowed = builder.build();
        final var baseRanges = baseLengths.getAllowedRanges();
        for (var range : allowed.asRanges()) {
            if (!baseRanges.encloses(range)) {
                throw new InvalidLengthConstraintException("Range %s is not a subset of parent constraint %s", range,
                    baseRanges);
            }
        }

        lengthConstraint = new ResolvedLengthConstraint(constraint, allowed);
        touch();
    }

    abstract @NonNull T buildType(LengthConstraint constraint);

    @Override
    final T buildType() {
        return buildType(lengthConstraint != null ? lengthConstraint : findLenghts());
    }

    abstract LengthConstraint typeLengthConstraints();

    private static Integer resolveLength(final Number unresolved, final Range<Integer> span) {
        return switch (unresolved) {
            case Integer integer -> integer;
            case UnresolvedNumber number -> number.resolveLength(span);
            default -> verifyNotNull(NumberUtil.converterTo(Integer.class)).apply(unresolved);
        };
    }

    private LengthConstraint findLenghts() {
        var ret = Optional.<LengthConstraint>empty();
        var wlk = getBaseType();
        while (wlk != null && ret.isEmpty()) {
            ret = wlk.getLengthConstraint();
            wlk = wlk.getBaseType();
        }

        return ret.orElse(typeLengthConstraints());
    }
}
