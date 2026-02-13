/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen.blk;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.binding.codegen.blk.BlockImpl.Block1;
import org.opendaylight.yangtools.binding.codegen.blk.BlockImpl.BlockN;

/**
 * Indirection between {@link Block} and internal implementations.
 */
sealed interface BlockImpl extends Block permits Block1, BlockN {
    /**
     * A {@link Block} implementation holding a single {@link NotBlock}.
     */
    record Block1(NotBlock item) implements BlockImpl {
        public Block1 {
            requireNonNull(item);
        }

        @Override
        public Appendable appendTo(final Appendable to, final int indent) throws IOException {
            item.appendTo(to, indent);
            return to.append('\n');
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder();
            item.appendTo(sb, 0);
            return sb.toString();
        }
    }

    /**
     * A {@link Block} implementation holding two or more {@link Item}s.
     */
    record BlockN(List<Item> items) implements BlockImpl {
        private static final String INDENT_STR = "    ";

        public BlockN {
            items = List.copyOf(items);
        }

        @Override
        public Appendable appendTo(final Appendable to, final int indent) throws IOException {
            final var it = items.iterator();
            if (it.hasNext()) {
                appendTo(to, indent, it);
            }
            return to.append('\n');
        }

        private static void appendTo(final Appendable to, final int indent, final Iterator<Item> it)
                throws IOException {
            var item = it.next();
            while (true) {
                // nested block get increased indent
                if (item instanceof Block block) {
                    block.appendTo(to.append(INDENT_STR), indent + 1);
                } else {
                    item.appendTo(to, indent);
                }

                // this is the last item: bail out
                if (!it.hasNext()) {
                    return;
                }

                // this has been a block and we have another item: be sure to apply indentation
                if (item instanceof Block) {
                    appendIndent(to, indent);
                }

                item = it.next();
            }
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder();
            appendTo(sb, 0);
            return sb.toString();
        }

        static void appendIndent(final Appendable to, final int indent) throws IOException {
            for (int i = 0; i < indent; ++i) {
                to.append(INDENT_STR);
            }
        }
    }
}