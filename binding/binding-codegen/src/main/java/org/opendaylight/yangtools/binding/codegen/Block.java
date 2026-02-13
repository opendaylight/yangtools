/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A basic text block. Every block is explicitly terminated by a newline ({@code '\n')).
 */
@NonNullByDefault
sealed interface Block extends Immutable, BlockBuilder.Item permits BlockBuilder.BlockImpl {
    /**
     * {@return a new {@link BlockBuilder}}
     */
    static BlockBuilder builder() {
        return new BlockBuilder();
    }

    default void appendTo(final StringBuilder sb) {
        appendTo(sb, 0);
    }

    default void appendTo(final StringBuilder sb, final int indent) {
        try {
            appendTo((Appendable) sb, indent);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default void appendTo(final Appendable to) throws IOException {
        appendTo(to, 0);
    }

    @Override
    void appendTo(Appendable to, int indent) throws IOException;
}
