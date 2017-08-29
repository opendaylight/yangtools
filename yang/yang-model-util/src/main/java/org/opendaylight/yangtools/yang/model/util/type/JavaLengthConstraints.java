/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

final class JavaLengthConstraints {
    private JavaLengthConstraints() {
        throw new UnsupportedOperationException();
    }

    private static final RangeSet<Integer> INTEGER_ALLOWED_RANGES =
            ImmutableRangeSet.of(Range.closed(0, Integer.MAX_VALUE));

    static final LengthConstraint INTEGER_SIZE_CONSTRAINTS = new LengthConstraint() {
        @Override
        public String getReference() {
            return null;
        }

        @Override
        public String getErrorMessage() {
            return null;
        }

        @Override
        public String getErrorAppTag() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public RangeSet<Integer> getAllowedRanges() {
            return INTEGER_ALLOWED_RANGES;
        }
    };
}
