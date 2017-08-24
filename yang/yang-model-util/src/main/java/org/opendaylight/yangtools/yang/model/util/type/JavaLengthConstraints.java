/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;

final class JavaLengthConstraints {
    private JavaLengthConstraints() {
        throw new UnsupportedOperationException();
    }

    static final RangeMap<Integer, ConstraintMetaDefinition> INTEGER_SIZE_CONSTRAINTS =
            ImmutableRangeMap.<Integer, ConstraintMetaDefinition>builder()
            .put(Range.closed(0, Integer.MAX_VALUE),
                BaseConstraints.newLengthConstraint(0, Integer.MAX_VALUE, Optional.absent(), Optional.absent()))
            .build();
}
