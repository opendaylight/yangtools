/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.errorprone.annotations.DoNotCall;
import java.io.IOException;
import java.util.List;

/**
 * A block that is a concatenation of two or more blocks.
 */
record BlockC(List<Block> blocks) implements Block {
    BlockC {
        blocks = List.copyOf(blocks);
    }

    @Override
    public void appendTo(final Appendable appendable) throws IOException {
        for (var block : blocks) {
            block.appendTo(appendable);
        }
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        for (var block : blocks) {
            block.appendTo(bb);
        }
    }

    @Override
    public String toRawString() {
        final var sb = new StringBuilder();
        for (var block : blocks) {
            block.appendTo(sb);
        }
        return sb.toString();
    }

    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    public String toString() {
        return toRawString();
    }
}
