/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
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

    @SuppressWarnings("ReturnValueIgnored")
    @SuppressFBWarnings(value = "DCN_NULLPOINTER_EXCEPTION", justification = "Internal NPE->IAE conversion")
    static void checkCollectionField(final @NonNull Class<?> requiredClass, final @NonNull String fieldName,
            final @Nullable Collection<?> collection) {
        if (collection != null) {
            try {
                collection.forEach(item -> requiredClass.cast(requireNonNull(item)));
            } catch (ClassCastException | NullPointerException e) {
                throw new IllegalArgumentException(
                    "Invalid input item for property \"" + requireNonNull(fieldName) + "\"", e);
            }
        }
    }
}
