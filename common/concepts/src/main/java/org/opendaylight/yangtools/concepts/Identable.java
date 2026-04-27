/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Something that can be appended into an {@link Appendable} while prefixing each line with some amount of indentation.
 */
@NonNullByDefault
public interface Identable {

    void appendTo(Appendable appendable) throws IOException;

    default void appendTo(final StringBuilder sb) {
        try {
            appendTo((Appendable) sb);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
