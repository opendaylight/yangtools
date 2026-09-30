/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.blk.Block;

/**
 * A {@link Block.Fragment} that can be appended to a {@link BlockBuilder}.
 */
@NonNullByDefault
@FunctionalInterface
interface BlockFragment extends Block.Fragment<BlockBuilder, BlockFragment> {
    @Override
    void appendTo(BlockBuilder bb);
}
