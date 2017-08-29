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
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

final class ResolvedLengthConstraint implements LengthConstraint {
    private final ConstraintMetaDefinition meta;
    private final RangeSet<Integer> ranges;

    ResolvedLengthConstraint(final ConstraintMetaDefinition meta, final RangeSet<Integer> ranges) {
        this.meta = requireNonNull(meta);
        this.ranges = ImmutableRangeSet.copyOf(ranges);
    }

    @Override
    public String getDescription() {
        return meta.getDescription();
    }

    @Override
    public String getErrorAppTag() {
        return meta.getErrorAppTag();
    }

    @Override
    public String getErrorMessage() {
        return meta.getErrorMessage();
    }

    @Override
    public String getReference() {
        return meta.getReference();
    }

    @Override
    public RangeSet<Integer> getAllowedRanges() {
        return ranges;
    }
}
