/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.DoNotCall;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
record Block1(String line) implements Block.OfOne {
    // one empty line
    static final Block1 EMPTY = new Block1("");

    Block1 {
        requireNonNull(line);
    }

    @Override
    public void appendTo(final Appendable appendable) throws IOException {
        appendable.append(line).append('\n');
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        if (line.isEmpty()) {
            bb.newLine();
        } else {
            bb.eol(line);
        }
    }

    @Override
    public String toRawString() {
        return line + '\n';
    }

    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    public String toString() {
        return toRawString();
    }
}
