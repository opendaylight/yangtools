/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Default implementation of {@link RpcError}.
 */
@NonNullByDefault
record DefaultRpcError(
        ErrorSeverity severity,
        ErrorType type,
        ErrorMessage message,
        @Nullable ErrorTag tag,
        @Nullable String applicationTag,
        @Nullable String info,
        @Nullable Throwable cause) implements RpcError, Serializable {
    DefaultRpcError {
        requireNonNull(severity);
        requireNonNull(type);
        requireNonNull(message);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(RpcError.class).omitNullValues()
            .add("severity", severity)
            .add("type", type)
            .add("message", message)
            .add("tag", tag)
            .add("applicationTag", applicationTag)
            .add("info", info)
            .add("cause", cause)
            .toString();
    }
}
