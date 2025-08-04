/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

// FIXME: YANGTOOLS-1150: this should go into yang-reactor-api
@NonNullByDefault
public enum ModelProcessingPhase {
    INIT(),

    STATEMENT_DEFINITION(INIT, ExecutionOrder.STATEMENT_DEFINITION),
    FULL_DECLARATION(STATEMENT_DEFINITION, ExecutionOrder.FULL_DECLARATION),
    EFFECTIVE_MODEL(FULL_DECLARATION, ExecutionOrder.EFFECTIVE_MODEL);

    /**
     * The concept of phase execution order, expressed as non-negative values.
     */
    public static final class ExecutionOrder {
        /**
         * Equivalent of a {@code null} {@link ModelProcessingPhase}.
         */
        public static final byte NULL                 = 0;
        /**
         * Corresponds to {@link ModelProcessingPhase#INIT}.
         */
        public static final byte INIT                 = 1;
        /**
         * Corresponds to {@link ModelProcessingPhase#STATEMENT_DEFINITION}.
         */
        public static final byte STATEMENT_DEFINITION = 2;
        /**
         * Corresponds to {@link ModelProcessingPhase#FULL_DECLARATION}.
         */
        public static final byte FULL_DECLARATION     = 3;
        /**
         * Corresponds to {@link ModelProcessingPhase#EFFECTIVE_MODEL}.
         */
        public static final byte EFFECTIVE_MODEL      = 4;

        private ExecutionOrder() {
            // Hidden on purpose
        }
    }

    /**
     * Members of this enum at their {@link #executionOrder} offset, with {@code 0} being reserved as {@code null}.
     */
    private static final ModelProcessingPhase[] BY_EXECUTION_ORDER;

    // BY_EXECUTION_ORDER initialization. The array has a semantic tie-in on ExectionOrder values, which has to follow
    // its rules. Since we are one-time indexing, let's make a thorough job of it and verify that everything is declared
    // as it should be.
    static {
        final ModelProcessingPhase[] values = values();
        final ModelProcessingPhase[] tmp = new ModelProcessingPhase[values.length + 1];

        for (ModelProcessingPhase phase : values) {
            final byte offset = phase.executionOrder;
            verify(offset > 0, "Invalid execution order in %s", phase);

            final ModelProcessingPhase existing = tmp[offset];
            verify(existing == null, "Execution order %s clash with %s", offset, existing);
            verify(tmp[offset - 1] == phase.previousPhase, "Illegal previous phase of %s", phase);
            tmp[offset] = phase;
        }

        BY_EXECUTION_ORDER = tmp;
    }

    private final @Nullable ModelProcessingPhase previousPhase;
    private final byte executionOrder;

    @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD",
        justification = "https://github.com/spotbugs/spotbugs/issues/743")
    // For INIT only
    ModelProcessingPhase() {
        previousPhase = null;
        executionOrder = ExecutionOrder.INIT;
    }

    ModelProcessingPhase(final ModelProcessingPhase previousPhase, final int executionOrder) {
        this.previousPhase = requireNonNull(previousPhase);
        this.executionOrder = (byte) executionOrder;
    }

    /**
     * Return the preceding phase, or null if this phase is the first one.
     *
     * @return Preceding phase, if there is one
     */
    public @Nullable ModelProcessingPhase getPreviousPhase() {
        return previousPhase;
    }

    /**
     * Determine whether this processing phase is implied to have completed by completion of some other phase.
     * Algebraically this means that other is not null and is either this phase or its {@link #getPreviousPhase()} chain
     * contains this phase.
     *
     * @param other Other phase
     * @return True if this phase completes no later than specified phase.
     */
    public boolean isCompletedBy(final @Nullable ModelProcessingPhase other) {
        return other != null && ordinal() <= other.ordinal();
    }

    /**
     * Return the execution order, which is a value in range {@code 1..127}.
     *
     * @return Execution order
     */
    public byte executionOrder() {
        return executionOrder;
    }

    /**
     * Return the {@link ModelProcessingPhase} corresponding to a {@link ExecutionOrder} value.
     *
     * @param executionOrder Execution order
     * @return Corresponding value, or null for {@link ExecutionOrder#NULL}
     * @throws IllegalArgumentException if the execution order is invalid
     */
    public static @Nullable ModelProcessingPhase ofExecutionOrder(final byte executionOrder) {
        try {
            return BY_EXECUTION_ORDER[executionOrder];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
