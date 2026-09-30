/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for {@linkplain Block.Builder} implementations.
 */
abstract sealed class AbstractBlockBuilder<B extends AbstractBlockBuilder<B>> implements Block.Builder
        permits BlockBuilder {
    /**
     * {@return {@code this}}
     */
    protected abstract @NonNull B self();

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    @Deprecated(forRemoval = true)
    public final String toString() {
        return toRawString();
    }
}
