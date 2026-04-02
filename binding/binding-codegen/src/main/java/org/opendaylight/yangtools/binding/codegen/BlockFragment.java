/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A part of a {@link Block}. All it can do is put itself into a {@link BlockBuilder}.
 */
@NonNullByDefault
@FunctionalInterface
interface BlockFragment {
    /**
     * Append this fragment to a {@link BlockBuilder}.
     *
     * @param bb the {@link BlockBuilder}
     */
    void appendTo(BlockBuilder bb);
}
