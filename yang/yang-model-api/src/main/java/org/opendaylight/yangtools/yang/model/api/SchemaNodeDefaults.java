/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Beta
@NonNullByDefault
public final class SchemaNodeDefaults {
    private SchemaNodeDefaults() {
        // Hidden on purpose
    }

    /**
     * Report unsupported {@link SchemaNode#getPath()} implementation. This method is guaranteed to throw a
     * RuntimeException.
     *
     * @param impl {@code this} object of invoking implementation
     * @throws NullPointerException if impl is null
     * @throws UnsupportedOperationException if impl is not null
     * @see SchemaNode#getPath()
     */
    // FIXME: 8.0.0: consider deprecating this method
    public static SchemaPath throwUnsupported(final Object impl) {
        throw new UnsupportedOperationException(impl.getClass() + " does not support SchemaNode.getPath()");
    }

    /**
     * Report unsupported {@link SchemaNode#getPath()} implementation if provided path is null.
     *
     * @param impl {@code this} object of invoking implementation
     * @throws NullPointerException if impl is null
     * @throws UnsupportedOperationException if path is null
     * @see SchemaNode#getPath()
     */
    // FIXME: 8.0.0: consider deprecating this method
    public static SchemaPath throwUnsupportedIfNull(final Object impl, final @Nullable SchemaPath path) {
        return path != null ? path : throwUnsupported(impl);
    }
}
