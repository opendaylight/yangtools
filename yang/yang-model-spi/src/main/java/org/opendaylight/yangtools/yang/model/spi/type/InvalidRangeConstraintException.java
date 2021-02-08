/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.RangeSet;

@Beta
public class InvalidRangeConstraintException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    private final RangeSet<?> offendingRangeConstraint;

    protected InvalidRangeConstraintException(final RangeSet<?> offendingConstraint, final String message) {
        super(message);
        this.offendingRangeConstraint = requireNonNull(offendingConstraint);
    }

    public InvalidRangeConstraintException(final RangeSet<?> offendingConstraint, final String format,
            final Object... args) {
        this(offendingConstraint, String.format(format, args));
    }

    public RangeSet<?> getOffendingRanges() {
        return offendingRangeConstraint;
    }
}
