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
import org.opendaylight.yangtools.concepts.PrettyString;

/**
 * A builder of {@link Block}s.
 */
@NonNullByDefault
final class BlockBuilder implements Mutable {
    /**
     * A single item in a {@link Block}.
     */
    sealed interface Item extends Immutable, PrettyString permits Block, NotBlock {
        // Nothing else
    }

    /**
     * Indirection between {@link Block} and internal implementations.
     */
    sealed interface BlockImpl extends Block permits Block1, BlockN {
        // Nothing else
    }

    /**
     * A {@link Block} implementation holding a single {@link NotBlock}.
     */
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

    /**
     * A {@link Block} implementation holding two or more {@link Item}s.
     */
    private record BlockN(List<Item> items) implements BlockImpl {
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
    }

    /**
     * An {@link Item} that is not a {@link Block}.
     */
    private sealed interface NotBlock extends Item permits Chr, Eol, Str, Txt {
        // Nothing else
    }

    /**
     * A single character.
     */
    private record Chr(char ch) implements NotBlock {
        // special case for ' '
        static final Chr SPACE = new Chr(' ');

        @Override
        public Appendable appendTo(final Appendable to, final int indent) throws IOException {
            return to.append(ch);
        }
    }

    /**
     * An end-of-line.
     */
    private static final class Eol implements NotBlock {
        static final Eol INSTANCE = new Eol();

        @Override
        public Appendable appendTo(final Appendable to, final int indent) throws IOException {
            return to.append('\n');
        }
    }

    /**
     * A string known not to contain new lines.
     */
    private record Str(String content) implements NotBlock {
        static final Str INDENT = new Str(INDENT_STR);

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

    /**
     * A string potentially containing new lines.
     */
    private record Txt(String content) implements NotBlock {
        Txt {
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
                appendIndent(to, indent);
                from = newLine;
            }
        }
    }

    private static final String INDENT_STR = "    ";

    private final ArrayList<Item> items = new ArrayList<>();

    private static void appendIndent(final Appendable to, final int indent) throws IOException {
        for (int i = 0; i < indent; ++i) {
            to.append(INDENT_STR);
        }
    }

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
     * Append a single character.
     *
     * @return this instance
     * @see #w()
     */
    BlockBuilder c(final char ch) {
        if (ch == ' ') {
            throw new IllegalArgumentException("use w() instead");
        }
        items.add(new Chr(ch));
        return this;
    }

    /**
     * Append an end-of-line.
     *
     * @return this instance
     */
    BlockBuilder eol() {
        // TODO: trim any preceding Chr.SPACE and Str.INDENT
        items.add(Eol.INSTANCE);
        return this;
    }

    /**
     * Append a level of indentation.
     *
     * @return this instance
     */
    BlockBuilder i() {
        items.add(Str.INDENT);
        return this;
    }

    /**
     * Append a complete line and terminate it. Equivalent to {@code s(content}.eol()}.
     *
     * @param content the {@link String}
     * @return this instance
     */
    BlockBuilder l(final String content) {
        return s(content).eol();
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
        items.add(new Txt(content));
        return this;
    }

    /**
     * Append a single space, equivalent to {@code c(' ')}. The name of this method is derived from {@code whitespace}.
     *
     * @return this instance
     */
    BlockBuilder w() {
        items.add(Chr.SPACE);
        return this;
    }

    /**
     * {@return a new {@link Block} capturing accumulated state}
     */
    Block build() {
        return switch (items.size()) {
            case 0 -> throw new IllegalStateException("no items");
            case 1 -> switch (items.getFirst()) {
                case Block block -> block;
                case NotBlock notBlock -> new Block1(notBlock);
            };
            default -> new BlockN(items);
        };
    }
}