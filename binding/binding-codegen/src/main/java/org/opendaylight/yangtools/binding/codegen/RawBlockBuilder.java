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
 * A raw {@link BlockBuilder}.
 */
@NonNullByDefault
final class RawBlockBuilder extends AbstractBlockBuilder<RawBlockBuilder, RawBlockFragment> {
    /**
     * Default constructor.
     */
    RawBlockBuilder() {
        // nothing else
    }

    @Override
    protected RawBlockBuilder self() {
        // TODO Auto-generated method stub
        return this;
    }
}
