package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
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
@Beta
public interface ReusableStreamReceiver extends NormalizedNodeStreamWriter {
    /**
     * Acquire the result of the last streaming session.
     *
     * @return Result of streaming.
     */
    NormalizedNode<?, ?> getResult();

    /**
     * Reset this writer to initial state.
     */
    void reset();
}
