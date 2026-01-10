/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.pipeline;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An exception reported by pipeline operations.
 */
// FIXME: NormalizationException, but bound to yang.common.InstanceIdentifier
@Beta
@NonNullByDefault
public final class PipelineException extends Exception {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public PipelineException(final String message) {
        super(requireNonNull(message));
    }

    public PipelineException(final String message, final @Nullable Exception cause) {
        super(requireNonNull(message), cause);
    }

    @Override
    @SuppressWarnings("null")
    public String getMessage() {
        return super.getMessage();
    }
}
