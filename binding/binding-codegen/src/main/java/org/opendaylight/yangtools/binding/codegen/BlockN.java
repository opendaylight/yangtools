/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.DoNotCall;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A block of three or more lines. Stored as a string fragment without the trailing newline.
 */
@NonNullByDefault
record BlockN(String str) implements Block {
    BlockN {
        if (str.length() < 2) {
            throw new VerifyException("bad str '" + str + "'");
        }
        requireNonNull(str);
    }

    @Override
    public void appendTo(final Appendable appendable) throws IOException {
        appendable.append(str).append('\n');
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        int from = 0;
        while (true) {
            final int nl = str.indexOf('\n', from);
            if (nl == -1) {
                bb.eol(str, from, str.length());
                return;
            }
            if (nl == from) {
                bb.newLine();
            } else {
                bb.eol(str, from, nl);
            }
            from = nl + 1;
        }
    }

    @Override
    public String toRawString() {
        return str + '\n';
    }

    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    public String toString() {
        return toRawString();
    }
}
