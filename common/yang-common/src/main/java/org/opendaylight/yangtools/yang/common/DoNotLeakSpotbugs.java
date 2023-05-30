/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods to hide operations that incur SpotBugs wrath used by public classes where we do not want to leak
 * {@link SuppressFBWarnings}.
 */
final class DoNotLeakSpotbugs {
    private DoNotLeakSpotbugs() {
        // Hidden on purpose
    }

    @SuppressFBWarnings(value = "ES_COMPARING_PARAMETER_STRING_WITH_EQ", justification = "Interning identity check")
    static @Nullable String internedString(final @NonNull String str) {
        final var interned = str.intern();
        return interned == str ? null : interned;
    }
}
