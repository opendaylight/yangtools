/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
@NonNullByDefault
public final class SchemaNodeDefaults {
    private SchemaNodeDefaults() {
        // Hidden on purpose
    }

    /**
     * Report unsupported {@link SchemaNode#getPath()} implementation. This method is guaranteed to throw an
     * {@link UnsupportedOperationException}.
     *
     * @param impl {@code this} object of invoking implementation
     * @return Nothing
     * @throws NullPointerException if {@code impl} is null
     * @throws UnsupportedOperationException always
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
     * @param path A schema path
     * @return {@code path} if non-null
     * @throws NullPointerException if {@code impl} is null
     * @throws UnsupportedOperationException if @{code path} is null
     * @see SchemaNode#getPath()
     */
    // FIXME: 8.0.0: consider deprecating this method
    public static SchemaPath throwUnsupportedIfNull(final Object impl, final @Nullable SchemaPath path) {
        return path != null ? path : throwUnsupported(impl);
    }

    /**
     * Extract {@link QName} from a path object.
     *
     * @param path Path handle
     * @return Extracted QName
     * @throws NullPointerException if {@code path} is null
     * @throws IllegalArgumentException if {@code path} is not supported
     */
    // FIXME: 8.0.0: consider deprecating this method
    public static QName extractQName(final Immutable path) {
        if (path instanceof QName) {
            return (QName) path;
        } else if (path instanceof SchemaPath) {
            return verifyNotNull(((SchemaPath) path).getLastComponent());
        } else {
            throw new IllegalArgumentException("Unhandled object " + path);
        }
    }

    /**
     * Extract {@link SchemaPath} from a path object.
     *
     * @param impl Implementation object
     * @param path Path handle
     * @return Extracted SchemaPath
     * @throws UnsupportedOperationException if {@code path} does not hold a SchemaPath
     */
    // FIXME: 8.0.0: consider deprecating this method
    public static SchemaPath extractPath(final Object impl, final Immutable path) {
        return path instanceof SchemaPath ? (SchemaPath) path : SchemaNodeDefaults.throwUnsupported(impl);
    }
}
