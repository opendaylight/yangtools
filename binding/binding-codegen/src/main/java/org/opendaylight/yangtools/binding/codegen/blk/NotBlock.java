/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen.blk;

import java.io.IOException;
import org.opendaylight.yangtools.binding.codegen.blk.BlockImpl.BlockN;

/**
 * An {@link Item} that is not a {@link Block}.
 */
sealed interface NotBlock extends Item {
    /**
     * A string known to end with a new line.
     */
    record Line(String content) implements NotBlock {
        static final Line EMPTY = new Line("");

        public Line {
            if (content.indexOf('\n') != -1) {
                throw new IllegalArgumentException("stray newline");
            }
        }

        @Override
        public Appendable appendTo(final Appendable to, final int indent) throws IOException {
            return to.append(content).append('\n');
        }
    }

    /**
     * A string known not to contain new lines.
     */
    record Str(String content) implements NotBlock {
        public Str {
            if (content.isEmpty()) {
                throw new IllegalArgumentException("empty string");
            }
            if (content.indexOf('\n') != -1) {
                throw new IllegalArgumentException("stray newline");
            }
        }

        @Override
        public Appendable appendTo(final Appendable to, final int indent) throws IOException {
            return to.append(content);
        }
    }


    /**
     * A string potentially containing new lines.
     */
    record Txt(String content) implements NotBlock {
        public Txt {
            if (content.isEmpty()) {
                throw new IllegalArgumentException("empty text block");
            }
        }

        @Override
        public Appendable appendTo(final Appendable to, final int indent) throws IOException {
            var from = 0;
            while (true) {
                // note: includes increment to include '\n'
                final var newLine = content.indexOf('\n', from) + 1;
                if (newLine == 0) {
                    return to.append(content, from, content.length());
                }

                to.append(content, from, newLine);
                BlockN.appendIndent(to, indent);
                from = newLine;
            }
        }
    }
}