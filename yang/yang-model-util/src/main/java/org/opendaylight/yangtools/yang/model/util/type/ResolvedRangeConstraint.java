/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.RangeSet;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

final class ResolvedRangeConstraint<T extends Number & Comparable<T>> implements RangeConstraint<T>, Immutable {
    private final ConstraintMetaDefinition meta;
    private final ImmutableRangeSet<T> ranges;

    ResolvedRangeConstraint(final ConstraintMetaDefinition meta, final RangeSet<T> ranges) {
        this.meta = requireNonNull(meta);
        this.ranges = ImmutableRangeSet.copyOf(ranges);
    }

    @Override
    public Optional<String> getDescription() {
        return meta.getDescription();
    }

    @Override
    public Optional<String> getErrorAppTag() {
        return meta.getErrorAppTag();
    }

    @Override
    public Optional<String> getErrorMessage() {
        return meta.getErrorMessage();
    }

    @Override
    public Optional<String> getReference() {
        return meta.getReference();
    }

    @Override
    public RangeSet<T> getAllowedRanges() {
        return ranges;
    }
}
