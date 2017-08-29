/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

@Beta
public abstract class UnresolvedNumber extends Number implements Immutable {
    private static final long serialVersionUID = 1L;
    private static final UnresolvedNumber MAX = new UnresolvedNumber() {
        private static final long serialVersionUID = 1L;

        @Override
        public Number resolveLength(final List<ValueRange> constraints) {
            return resolve(constraints.get(constraints.size() - 1).upperBound());
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
        public Number resolveLength(final List<ValueRange> constraints) {
            return resolve(constraints.get(0).lowerBound());
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

    private static Number resolve(final Number number) {
        Preconditions.checkArgument(!(number instanceof UnresolvedNumber));
        return number;
    }

    public abstract Number resolveLength(List<ValueRange> constraints);

    public abstract Number resolveRange(List<RangeConstraint> constraints);

    @Override
    public abstract String toString();
}
