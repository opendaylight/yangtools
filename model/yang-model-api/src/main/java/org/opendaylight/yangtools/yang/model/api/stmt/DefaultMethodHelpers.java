/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Simple helper methods used in default implementations.
 */
final class DefaultMethodHelpers {
    private DefaultMethodHelpers() {
        // Hidden on purpose
    }

    static <E> @NonNull Optional<E> filterOptional(final @NonNull Optional<?> optional, final @NonNull Class<E> type) {
        return optional.filter(type::isInstance).map(type::cast);
    }
}
