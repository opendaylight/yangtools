/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Utility marker interface for {@link NormalizedNodeStreamWriter} implementations which can be reused multiple times
 * and can expose a {@link NormalizedNode} result of each complete streaming use.
 *
 * <p>
 * An example of use would be:
 * <pre>
 *   ReusableStreamReceiver writer;
 *   final NormalizedNode result;
 *
 *   try {
 *       // pipe events into writer:
 *       writer.startContainer(...);
 *       ...
 *
 *       result = writer.getResult();
 *   } finally {
 *       writer.reset();
 *   }
 * </pre>
 *
 * <p>
 * Note the writer should always be {@link #reset()} in a {@code finally} block, so that any streaming state is
 * properly discarded.
 */
public interface ReusableStreamReceiver extends NormalizedNodeStreamWriter {
    /**
     * Acquire the result of the last streaming session.
     *
     * @return Result of streaming, or {@code null} if not result is present.
     */
    @Nullable NormalizationResult<?> result();

    /**
     * Reset this writer to initial state.
     */
    void reset();
}
