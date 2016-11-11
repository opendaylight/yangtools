/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import javax.annotation.Nonnull;

/**
 * Simple integer-to-StatementContextBase map optimized for size and restricted in scope of operations.
 *
 * @author Robert Varga
 */
abstract class StatementMap {
    private static final class Empty extends StatementMap {
        @Override
        StatementContextBase<?, ?, ?> get(final int index) {
            return null;
        }

        @Override
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> object) {
            return new Singleton(index, object);
        }
    }

    private static final class Regular extends StatementMap {
        private StatementContextBase<?, ?, ?>[] elements;

        Regular(final int index1, final StatementContextBase<?, ?, ?> object1, final int index2,
                final StatementContextBase<?, ?, ?> object2) {
            elements = new StatementContextBase<?, ?, ?>[Math.max(index1, index2) + 1];
            elements[index1] = Preconditions.checkNotNull(object1);
            elements[index2] = Preconditions.checkNotNull(object2);
        }

        @Override
        StatementContextBase<?, ?, ?> get(final int index) {
            if (index >= elements.length) {
                return null;
            }

            return elements[index];
        }

        @Override
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> object) {
            Preconditions.checkNotNull(object);
            if (index < elements.length) {
                Preconditions.checkArgument(elements[index] == null);
            } else {
                elements = Arrays.copyOf(elements, index + 1);
            }

            elements[index] = object;
            return this;
        }
    }

    private static final class Singleton extends StatementMap {
        private final StatementContextBase<?, ?, ?> object;
        private final int index;

        Singleton(final int index, final StatementContextBase<?, ?, ?> object) {
            Preconditions.checkArgument(index >= 0);
            this.object = Preconditions.checkNotNull(object);
            this.index = index;
        }

        @Override
        StatementContextBase<?, ?, ?> get(final int index) {
            return this.index == index ? object : null;
        }

        @Override
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> object) {
            Preconditions.checkArgument(index != this.index);
            return new Regular(this.index, this.object, index, object);
        }
    }

    private static final StatementMap EMPTY = new Empty();

    static StatementMap empty() {
        return EMPTY;
    }

    abstract StatementContextBase<?, ?, ?> get(int index);
    abstract @Nonnull StatementMap put(int index, @Nonnull StatementContextBase<?, ?, ?> object);
}
