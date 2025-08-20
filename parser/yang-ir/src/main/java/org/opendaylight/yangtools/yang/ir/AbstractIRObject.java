/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import com.google.common.base.MoreObjects;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

@NonNullByDefault
abstract sealed class AbstractIRObject implements Immutable permits IRArgument, IRKeyword, IRStatement {
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("fragment", toYangFragment(new StringBuilder())).toString();
    }

    final StringBuilder toYangFragment(final StringBuilder sb) {
        try {
            toYangFragment((Appendable) sb);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb;
    }

    abstract Appendable toYangFragment(Appendable appendable) throws IOException;
}
