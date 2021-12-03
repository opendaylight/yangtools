/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;

/**
 * Abstract base class for {@link ResolvedLengthConstraint} and {@link ResolvedRangeConstraint}.
 *
 * @param <T> type of constraint
 */
abstract class AbstractConstraint<T extends Number & Comparable<T>> implements ConstraintMetaDefinition {
    private final ConstraintMetaDefinition meta;
    private final Object ranges;

    AbstractConstraint(final ConstraintMetaDefinition meta, final RangeSet<T> ranges) {
        this.meta = requireNonNull(meta);

        final var tmp = ranges.asRanges();
        if (tmp.size() == 1) {
            this.ranges = tmp.iterator().next();
        } else {
            this.ranges = ImmutableRangeSet.copyOf(ranges);
        }
    }

    @Override
    public final Optional<String> getDescription() {
        return meta.getDescription();
    }

    @Override
    public final Optional<String> getErrorAppTag() {
        return meta.getErrorAppTag();
    }

    @Override
    public final Optional<String> getErrorMessage() {
        return meta.getErrorMessage();
    }

    @Override
    public final Optional<String> getReference() {
        return meta.getReference();
    }

    @SuppressWarnings("unchecked")
    final @NonNull ImmutableRangeSet<T> ranges() {
        if (ranges instanceof ImmutableRangeSet) {
            return (ImmutableRangeSet<T>) ranges;
        }
        verify(ranges instanceof Range, "Unexpected range object %s", ranges);
        return ImmutableRangeSet.of((Range<T>) ranges);
    }
}
