/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Universal builder of a string block. A block is composed of one or more lines, concatenated using {@code '\n'}.
 *
 * <p>Currently it is just a {@link StringConcatenation} but it will expand as we integrate more users.
 */
final class BlockBuilder implements Mutable {
    // FIXME: remove use of StringConcatenation
    private final StringConcatenation sc = new StringConcatenation("\n");

    void append(final @NonNull BlockBuilder bb) {
        verify(bb != this);
        sc.append(bb.sc);
    }

    @NonNull String toRawString() {
        return verifyNotNull(sc.toString());
    }

    String toJavadocBlock() {
        return sc.isEmpty() ? "" : BaseTemplate.wrapToDocumentation(toRawString());
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
