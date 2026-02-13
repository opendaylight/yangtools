/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A builder of {@link Block}s.
 */
@NonNullByDefault
final class BlockBuilder implements Mutable {

    sealed interface Item extends Immutable permits Block, NotBlock {

        Appendable appendTo(Appendable to, int indent) throws IOException;
    }

    // indirection to keep all implementations private
    sealed interface BlockImpl extends Block {
        // Nothing else
    }

    private sealed interface NotBlock extends Item permits Str, TextBlock {
        // Nothing else
    }

    private record Block1(NotBlock item) implements BlockImpl {
        Block1 {
            requireNonNull(item);
        }

        @Override
        public Appendable appendTo(final Appendable to, final int indent) throws IOException {
            item.appendTo(to, indent);
            return to.append('\n');
        }
    }

    private record BlockN(List<Item> items) implements BlockImpl {
        static final BlockN EMPTY = new BlockN(List.of());

        BlockN {
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
                item.appendTo(to, indent);

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
    }

    private record Str(String content) implements NotBlock {
        Str {
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

    private record TextBlock(String content) implements NotBlock {
        TextBlock {
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
                    break;
                }

                to.append(content, from, newLine);
                appendIndent(to, indent);
                from = newLine;
            }
            return to.append(content, from, content.length());
        }
    }

    private final ArrayList<Item> items = new ArrayList<>();

    /**
     * Append a {@link Block}.
     *
     * @param block the {@link Block}
     * @return this instance
     */
    BlockBuilder b(final Block block) {
        items.add(requireNonNull(block));
        return this;
    }

    /**
     * Append an end-of-line.
     *
     * @return this instance
     */
    BlockBuilder eol() {
        items.add(BlockN.EMPTY);
        return this;
    }

    /**
     * Append a {@link String}, which is known to not contain new lines.
     *
     * @param content the {@link String}
     * @return this instance
     */
    BlockBuilder s(final String content) {
        items.add(new Str(content));
        return this;
    }

    /**
     * Append a {@link String} text block, which may or may not contain new lines. It must not end with a new line.
     *
     * @param content the {@link String}
     * @return this instance
     */
    BlockBuilder t(final String content) {
        items.add(new TextBlock(content));
        return this;
    }

    /**
     * {@return a new {@link Block} capturing accumulated state}
     */
    Block build() {
        return switch (items.size()) {
            case 0 -> throw new IllegalStateException("No items");
            case 1 ->
                switch (items.getFirst()) {
                    case Block block -> block;
                    case NotBlock notBlock -> new Block1(notBlock);
                };
            default -> new BlockN(items);
        };
    }

    private static void appendIndent(final Appendable to, final int indent) throws IOException {
        for (int i = 0; i < indent; ++i) {
            to.append("    ");
        }
    }
}