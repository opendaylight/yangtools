/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 * The task of driving a {@link NormalizedNodeStreamWriter}'s callbacks to reconstruct a NormalizedNode document.
 * Execution of this task may be attempted exactly once, via {@link #execute()} method. Instances of this class are
 * acquired in an implementation-specific manner, but implementations are advised to take advantage of staged builder
 * pattern, with the final stages being {@link RequireWriterBuilderStage} and {@link FinalBuilderStage}.
 */
@Beta
@NonNullByDefault
public abstract class NormalizedNodeStreamPushTask {
    private static final VarHandle EXECUTED;

    static {
        try {
            EXECUTED = MethodHandles.lookup()
                .findVarHandle(NormalizedNodeStreamPushTask.class, "executed", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private volatile boolean executed;

    /**
     * Execute this task and report any {@link PathArgument}s between the task initialization point and the actual
     * NormalizedNode produced by the associated writer.
     *
     * <p>
     * This difference may exist because of how {@link YangInstanceIdentifier} and {@link NormalizedNode} hierarchy
     * relates to YANG definition (as expressed by {@link EffectiveStatementInference}) and various external encoding
     * formats (like JSON, XML and others).
     *
     * <p>
     * As a concrete example, a RESTCONF north-bound endpoint works on two pieces of information:
     * <ol>
     *   <li>an {@code instance identifier} encoded in the URL</li>
     *   <li>a document encoded by the request body</li>
     * </ol>
     * Under usual YANG encoding rules the difference between the instance identifier and the document root is either
     * zero (in case of a PUT method) or one (in case of a POST method) step along the YANG data tree axis.
     * Unfortunately this single step can legally be represented by a sequence of {@link PathArgument}s.
     *
     * <p>
     * The overall contract is that, in terms of instantiated tree addressing, the {@link YangInstanceIdentifier}
     * corresponding to the resulting {@link NormalizedNode} is formed by:
     * <ol>
     *   <li>taking the initial {@link PathArgument} segment as known by means outside of this specification, for
     *       example by interpreting RESTCONF request URL, then</li>
     *   <li>appending the sequence returned from this method, and finally<li>
     *   <li>appending the {@link NormalizedNode#getIdentifier()} of the {@link NormalizedNode} produced by the
     *       associated {@link NormalizedNodeStreamWriter}
     * </ol>
     *
     * @return Sequence of {@link PathArgument}s between
     * @throws IOException if execution encounters an error
     * @throws IllegalStateException if this task has already been executed
     */
    @SuppressWarnings("checkstyle:illegalCatch")
    public final List<PathArgument> execute() throws IOException {
        if (!EXECUTED.compareAndSet(this, false, true)) {
            throw new IllegalStateException(this + " has already been executed");
        }

        try {
            return List.copyOf(executeImpl());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to execute task " + this, e);
        }
    }

    protected abstract List<PathArgument> executeImpl() throws Exception;

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("executed", executed);
    }

    /**
     * Second-to-last stage in building a {@link NormalizedNodeStreamPushTask}. In order to progress to the last stage,
     * use {@link #withWriter(NormalizedNodeStreamWriter)}.
     */
    public interface RequireWriterBuilderStage {
        /**
         * Return the last stage of building a {@link NormalizedNodeStreamPushTask}, driving the specified
         * {@link NormalizedNodeStreamWriter}.
         *
         * @param writer A {@link NormalizedNodeStreamWriter}
         * @return A {@link FinalBuilderStage}
         * @throws NullPointerException if {@code writer} is {@code null}
         * @throws IllegalArgumentException if {@code writer} does not meet implementation-specific criteria of this
         *                                  builder
         */
        FinalBuilderStage withWriter(NormalizedNodeStreamWriter writer);
    }

    /**
     * Last stage in building a {@link NormalizedNodeStreamPushTask}. The process is completed by invoking
     * {@link #build()}.
     */
    public interface FinalBuilderStage {
        /**
         * Return a new {@link NormalizedNodeStreamPushTask} based on the state collected in this builder.
         *
         * @return A new {@link NormalizedNodeStreamPushTask}.
         */
        NormalizedNodeStreamPushTask build();
    }
}
