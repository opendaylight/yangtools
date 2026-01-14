/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.ElementCountMatcher;

/**
 * Common interface for list-like nodes, which can optionally have constraints on the number of direct children.
 */
@NonNullByDefault
public interface ElementCountAware {
    /**
     * {@return an {@code ElementCountMatcher} enforcing the constraint on number of elements, or {@code null} if no
     * enforcement is needed}
     */
    @Nullable ElementCountMatcher elementCountMatcher();
}
