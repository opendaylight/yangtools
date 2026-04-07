/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.DoNotCall;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link Block} with non-zero indentation.
 */
@NonNullByDefault
record BlockI(Block delegate, int level) implements Block {
    BlockI {
        if (delegate.level() != 0) {
            throw new VerifyException("indented delegate");
        }
        if (level < 1) {
            throw new VerifyException("bad level " + level);
        }
    }

    @Override
    public Block withLevel(final int newLevel) {
        if (newLevel == 0) {
            return delegate;
        }
        return newLevel == level ? this : new BlockI(delegate, newLevel);
    }

    @Override
    public void appendTo(final Appendable appendable, final int depth) throws IOException {
        delegate.appendTo(appendable, depth + level);
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        delegate.appendTo(bb);
    }

    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    public String toString() {
        return toRawString();
    }
}
