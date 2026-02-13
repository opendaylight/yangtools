/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen.blk;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import java.util.ArrayList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.codegen.blk.BlockImpl.Block1;
import org.opendaylight.yangtools.binding.codegen.blk.BlockImpl.BlockN;
import org.opendaylight.yangtools.binding.codegen.blk.NotBlock.Line;
import org.opendaylight.yangtools.binding.codegen.blk.NotBlock.Str;

@NonNullByDefault
abstract sealed class AbstractBuilder implements Block.Builder {
    private abstract static sealed class WithItems extends AbstractBuilder {
        final ArrayList<Item> items;

        WithItems(final ArrayList<Item> items) {
            if (items.isEmpty()) {
                throw new IllegalArgumentException("empty items");
            }
            this.items = items;
        }
    }

    private abstract static sealed class WithoutItems extends AbstractBuilder {
        // NothingElse
    }

    static final class Empty extends WithoutItems {
        static final Empty INSTANCE = new Empty();

        private Empty() {
            // Hidden on purpose
        }

        @Override
        AbstractBuilder append(final char ch) {
            return new OnlyStringBuilder(new StringBuilder().append(ch));
        }

        @Override
        AbstractBuilder append(final String content) {
            return new OnlyString(content);
        }

        @Override
        public AbstractBuilder blk(final Block block) {
            return OnlyItems.of(block);
        }

        @Override
        public AbstractBuilder nl() {
            return OnlyItems.of(Line.EMPTY);
        }

        @Override
        public Block build() {
            throw new IllegalStateException("empty block");
        }
    }

    private static final class ItemsAndString extends WithItems {
        private final String str;

        ItemsAndString(final ArrayList<Item> items, final String str) {
            super(items);
            if (str.isEmpty()) {
                throw new IllegalArgumentException("empty string");
            }
            this.str = str;
        }

        @Override
        AbstractBuilder append(final char ch) {
            return new ItemsAndStringBuilder(items, new StringBuilder(str).append(ch));
        }

        @Override
        AbstractBuilder append(final String content) {
            return new ItemsAndStringBuilder(items, new StringBuilder(str).append(content));
        }

        @Override
        public AbstractBuilder blk(final Block block) {
            items.add(requireNonNull(block));
            return this;
        }

        @Override
        public AbstractBuilder nl() {
            items.add(new Line(str));
            return new OnlyItems(items);
        }

        @Override
        public Block build() {
            items.add(new Str(str));
            return new BlockN(items);
        }
    }

    private static final class ItemsAndStringBuilder extends WithItems {
        private final StringBuilder sb;

        ItemsAndStringBuilder(final ArrayList<Item> items, final StringBuilder sb) {
            super(items);
            this.sb = requireNonNull(sb);
        }

        @Override
        AbstractBuilder append(final char ch) {
            sb.append(ch);
            return this;
        }

        @Override
        AbstractBuilder append(final String content) {
            sb.append(content);
            return this;
        }

        @Override
        public AbstractBuilder blk(final Block block) {
            throw new IllegalStateException("cannot append block after string");
        }

        @Override
        public AbstractBuilder nl() {
            items.add(new Line(sb.toString()));
            return new OnlyItems(items);
        }

        @Override
        public Block build() {
            items.add(new Str(sb.toString()));
            return new BlockN(items);
        }
    }

    private static final class OnlyItems extends WithItems {
        OnlyItems(final ArrayList<Item> items) {
            super(items);
        }

        static OnlyItems of(final Item item) {
            final var items = new ArrayList<Item>();
            items.add(requireNonNull(item));
            return new OnlyItems(items);
        }

        @Override
        AbstractBuilder append(final char ch) {
            return new ItemsAndStringBuilder(items, new StringBuilder().append(ch));
        }

        @Override
        AbstractBuilder append(final String content) {
            return new ItemsAndString(items, content);
        }

        @Override
        public AbstractBuilder blk(final Block block) {
            throw new IllegalStateException("cannot append block after string");
        }

        @Override
        public AbstractBuilder nl() {
            items.add(Line.EMPTY);
            return this;
        }

        @Override
        public Block build() {
            return items.size() != 1 ? new BlockN(items) : switch (items.getFirst()) {
                case Block block -> block;
                case NotBlock notBlock -> new Block1(notBlock);
            };
        }
    }

    private static final class OnlyString extends WithoutItems {
        private final String str;

        OnlyString(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        AbstractBuilder append(final char ch) {
            return new OnlyStringBuilder(new StringBuilder(str).append(ch));
        }

        @Override
        AbstractBuilder append(final String content) {
            return new OnlyStringBuilder(new StringBuilder(str).append(content));
        }

        @Override
        public AbstractBuilder blk(final Block block) {
            throw new IllegalStateException("cannot append block after string");
        }

        @Override
        public AbstractBuilder nl() {
            return OnlyItems.of(new Line(str));
        }

        @Override
        public Block build() {
            return new Block1(new Str(str));
        }
    }

    private static final class OnlyStringBuilder extends WithoutItems {
        private final StringBuilder sb;

        OnlyStringBuilder(final StringBuilder sb) {
            this.sb = requireNonNull(sb);
        }

        @Override
        AbstractBuilder append(final char ch) {
            sb.append(ch);
            return this;
        }

        @Override
        AbstractBuilder append(final String content) {
            sb.append(content);
            return this;
        }

        @Override
        public AbstractBuilder blk(final Block block) {
            throw new IllegalStateException("cannot append block after string");
        }

        @Override
        public AbstractBuilder nl() {
            return OnlyItems.of(new Line(sb.toString()));
        }

        @Override
        public Block1 build() {
            return new Block1(new Str(sb.toString()));
        }
    }

    @CheckReturnValue
    abstract AbstractBuilder append(char ch);

    @CheckReturnValue
    abstract AbstractBuilder append(String content);

    @Override
    public final AbstractBuilder at() {
        return append('@');
    }

    @Override
    public final AbstractBuilder lb() {
        return append('{');
    }

    @Override
    public final AbstractBuilder rb() {
        return append('}');
    }

    @Override
    public final AbstractBuilder lp() {
        return append('(');
    }

    @Override
    public final AbstractBuilder rp() {
        return append(')');
    }

    @Override
    public final AbstractBuilder sp() {
        return append(' ');
    }

    @Override
    public final Block.Builder line(final String content) {
        return str(content).nl();
    }

    /**
     * Append a complete line from a {@link StringBuilder}. and terminate it. Mostly equivalent to
     * {@code str(sb.toString()}.nl()}, but also resets the {@link StringBuilder}.
     *
     * @param sb the {@link StringBuilder}
     * @return this instance
     */
    @CheckReturnValue
    final Block.Builder line(final StringBuilder sb) {
        final var ret = line(sb.toString());
        sb.setLength(0);
        return ret;
    }

    @Override
    public final AbstractBuilder str(final String content) {
        return content.isEmpty() ? this : append(content);
    }

    @Override
    public final AbstractBuilder str(final String content, final int beginIndex, final int endIndex) {
        // FIXME: do not use substring()
        return str(content.substring(beginIndex, endIndex));
    }

    @Override
    public final Block.Builder txt(final String content) {
        final var length = content.length();
        if (length == 0) {
            return this;
        }

        Block.Builder self = this;
        var from = 0;
        while (true) {
            final var nl = content.indexOf('\n', from);
            if (nl == -1) {
                break;
            }
            self = self.str(content, from, nl).nl();
            from = nl + 1;
        }

        return from == length ? self : self.str(content, from, length).nl();
    }
}
