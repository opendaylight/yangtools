/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Universal builder of a string block. A block is composed of one or more lines, concatenated using {@code '\n'}.
 *
 * <p>Currently it is just a {@link StringConcatenation} but it will expand as we integrate more users.
 */
// FIXME: internalize StringConcatenation
final class BlockBuilder extends StringConcatenation implements Mutable {
    /**
     * Default constructor. Uses {@code "\n"} as {@link #getLineDelimiter()} instead of the platform-dependent
     * {@link StringConcatenation#DEFAULT_LINE_DELIMITER}.
     */
    BlockBuilder() {
        super("\n");
    }

    @Override
    @Deprecated(forRemoval = true)
    public void append(final StringConcatenation concat) {
        if (concat instanceof BlockBuilder bb) {
            append(bb);
        } else {
            super.append(concat);
        }
    }

    void append(final @NonNull BlockBuilder bb) {
        super.append(requireNonNull(bb));
    }

    @NonNull String toRawString() {
        return verifyNotNull(super.toString());
    }

    String toJavadocBlock() {
        return isEmpty() ? "" : BaseTemplate.wrapToDocumentation(toRawString());
    }

    /**
     * {@return the result of {@link #toRawString()}}
     * @deprecated use {@link #toRawString()} directly
     */
    @Override
    @Deprecated(forRemoval = true)
    public String toString() {
        return toRawString();
    }
}
