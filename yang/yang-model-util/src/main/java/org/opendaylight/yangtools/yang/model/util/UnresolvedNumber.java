/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import java.util.List;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

@Beta
public abstract class UnresolvedNumber extends Number implements Immutable {
    private static final long serialVersionUID = 1L;
    private static final UnresolvedNumber MAX = new UnresolvedNumber() {
        private static final long serialVersionUID = 1L;

        @Override
        public Integer resolveLength(final Range<Integer> range) {
            return resolve(range.upperEndpoint());
        }

        @Override
        public Number resolveRange(final List<RangeConstraint> constraints) {
            return resolve(constraints.get(constraints.size() - 1).getMax());
        }

        @Override
        public String toString() {
            return "max";
        }

        private Object readResolve() {
            return MAX;
        }
    };

    private static final UnresolvedNumber MIN = new UnresolvedNumber() {
        private static final long serialVersionUID = 1L;

        @Override
        public Integer resolveLength(final Range<Integer> range) {
            return resolve(range.lowerEndpoint());
        }

        @Override
        public Number resolveRange(final List<RangeConstraint> constraints) {
            return resolve(constraints.get(0).getMin());
        }

        @Override
        public String toString() {
            return "min";
        }

        private Object readResolve() {
            return MIN;
        }
    };

    public static UnresolvedNumber min() {
        return MIN;
    }

    public static UnresolvedNumber max() {
        return MAX;
    }

    @Override
    public final int intValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final long longValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final float floatValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final double doubleValue() {
        throw new UnsupportedOperationException();
    }

    private static <T> T resolve(final T number) {
        Preconditions.checkArgument(!(number instanceof UnresolvedNumber));
        return number;
    }

    public abstract Integer resolveLength(Range<Integer> range);

    public abstract Number resolveRange(List<RangeConstraint> constraints);

    @Override
    public abstract String toString();
}
