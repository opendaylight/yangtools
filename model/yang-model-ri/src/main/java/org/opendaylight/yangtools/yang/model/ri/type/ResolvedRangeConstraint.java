/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import com.google.common.collect.RangeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

final class ResolvedRangeConstraint<T extends Number & Comparable<T>>
        extends AbstractConstraint<@NonNull T> implements RangeConstraint<T> {
    ResolvedRangeConstraint(final ConstraintMetaDefinition meta, final RangeSet<T> ranges) {
        super(meta, ranges);
    }

    @Override
    public RangeSet<@NonNull T> getAllowedRanges() {
        return ranges();
    }
}
