/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.blk;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link Block.Fragment} associated with {@link RawBlockBuilder}.
 *
 * @since 16.1.1
 */
@NonNullByDefault
@FunctionalInterface
public interface RawBlockFragment extends Block.Fragment<RawBlockBuilder, RawBlockFragment> {
    @Override
    default void appendTo(final RawBlockBuilder bb) {
        appendRaw(bb);
    }

    /**
     * Append this fragment to a {@link Builder}.
     *
     * @param bb the block builder
     */
    void appendRaw(Block.Builder bb);
}
