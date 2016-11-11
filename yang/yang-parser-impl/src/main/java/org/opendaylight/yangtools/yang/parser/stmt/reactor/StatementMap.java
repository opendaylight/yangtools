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
            return index == 0 ? new Singleton(object) : new Regular(index, object);
        }
    }

    private static final class Regular extends StatementMap {
        private StatementContextBase<?, ?, ?>[] elements;

        Regular(final int index, final StatementContextBase<?, ?, ?> object) {
            elements = new StatementContextBase<?, ?, ?>[index + 1];
            elements[index] = Preconditions.checkNotNull(object);
        }

        Regular(final StatementContextBase<?, ?, ?> object0, final int index,
                final StatementContextBase<?, ?, ?> object) {
            elements = new StatementContextBase<?, ?, ?>[index + 1];
            elements[0] = Preconditions.checkNotNull(object0);
            elements[index] = Preconditions.checkNotNull(object);
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
            if (index < elements.length) {
                Preconditions.checkArgument(elements[index] == null);
            } else {
                elements = Arrays.copyOf(elements, index + 1);
            }

            elements[index] = Preconditions.checkNotNull(object);
            return this;
        }
    }

    private static final class Singleton extends StatementMap {
        private final StatementContextBase<?, ?, ?> object;

        Singleton(final StatementContextBase<?, ?, ?> object) {
            this.object = Preconditions.checkNotNull(object);
        }

        @Override
        StatementContextBase<?, ?, ?> get(final int index) {
            return index == 0 ? object : null;
        }

        @Override
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> object) {
            Preconditions.checkArgument(index != 0);
            return new Regular(this.object, index, object);
        }
    }

    private static final StatementMap EMPTY = new Empty();

    static StatementMap empty() {
        return EMPTY;
    }

    abstract StatementContextBase<?, ?, ?> get(int index);
    abstract @Nonnull StatementMap put(int index, @Nonnull StatementContextBase<?, ?, ?> object);
}
