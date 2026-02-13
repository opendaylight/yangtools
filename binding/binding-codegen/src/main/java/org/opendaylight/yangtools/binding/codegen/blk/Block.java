/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen.blk;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A basic text block. Every block is explicitly terminated by a newline ({@code '\n'}).
 */
@NonNullByDefault
public sealed interface Block extends Immutable, Item permits BlockImpl {
    /**
     * {@return a new {@link AbstractBuilder}}
     */
    static Builder builder() {
        return AbstractBuilder.Empty.INSTANCE;
    }

    @Override
    Appendable appendTo(Appendable to, int indent) throws IOException;

    /**
     * A builder of {@link Block}s.
     */
    sealed interface Builder extends Mutable permits AbstractBuilder {
        /**
         * Append a {@code '@'}.
         *
         * @return this instance
         */
        @CheckReturnValue
        Builder at();

        /**
         * Append a {@code '\n'}.
         *
         * @return this instance
         */
        @CheckReturnValue
        Builder nl();

        /**
         * Append a {@code '&#123;'}.
         *
         * @return this instance
         */
        @CheckReturnValue
        Builder lb();

        /**
         * Append a {@code '&#124;'}.
         *
         * @return this instance
         */
        @CheckReturnValue
        Builder rb();

        /**
         * Append a {@code '('}.
         *
         * @return this instance
         */
        @CheckReturnValue
        Builder lp();

        /**
         * Append a {@code ')'}.
         *
         * @return this instance
         */
        @CheckReturnValue
        Builder rp();

        /**
         * Append a {@code ' '}.
         *
         * @return this instance
         */
        @CheckReturnValue
        Builder sp();

        /**
         * Append a {@link Block}.
         *
         * @param block the {@link Block}
         * @return this instance
         */
        @CheckReturnValue
        Builder blk(Block block);

        /**
         * Append a complete line and terminate it. Equivalent to {@code str(content}.nl()}.
         *
         * @param content the {@link String}
         * @return this instance
         */
        @CheckReturnValue
        Builder line(String content);

        /**
         * Append a {@link String} which is known to not contain new lines.
         *
         * @param content the {@link String}
         * @return this instance
         */
        @CheckReturnValue
        Builder str(String content);

        /**
         * Append a {@link String} range which is known to not contain new lines.
         *
         * @param str the {@link String}
         * @param beginIndex the first included index
         * @param endIndex the first excluded index;
         * @return this instance
         */
        @CheckReturnValue
        Builder str(String str, int beginIndex, int endIndex);

        /**
         * Append a {@link String} text block, which may or may not contain new lines. It must not end with a new line.
         *
         * @param content the {@link String}
         * @return this instance
         */
        @CheckReturnValue
        Builder txt(String content);

        /**
         * {@return a new {@link Block} capturing accumulated state}
         */
        @CheckReturnValue
        Block build();
    }
}
