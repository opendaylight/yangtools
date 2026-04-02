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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

// FIXME: YANGTOOLS-1150: this should go into yang-reactor-api
public enum ModelProcessingPhase {
    INIT(),

    /**
     * Cross-source relationship resolution phase.
     *
     * <p>In this phase of processing only statements which affects cross-source relationship (e.g. imports / includes)
     * are processed.
     *
     * <p>At end of this phase all source related contexts should be bind to their imports and includes to allow
     * visibility of custom defined statements in subsequent phases. Most notably all
     * {@link ParserNamespace#readOnly(String)} namespaces are initialized.
     */
    SOURCE_LINKAGE(INIT, ExecutionOrder.SOURCE_LINKAGE),
    // FIXME: YANGTOOLS-1840: this phase should include all statements except TypeStatement and DefaultStatement
    // FIXME: document the following namespaces as frozen:
    //        - ParserNamespaces.EXTENSION
    //        - ParserNamespaces.FEATURE
    //        - ParserNamespaces.GROUPING
    //        - ParserNamespaces.IDENTITY
    //        - ParserNamespaces.TYPE
    STATEMENT_DEFINITION(SOURCE_LINKAGE, ExecutionOrder.STATEMENT_DEFINITION),

    // FIXME: YANGTOOLS-1840: this phase should add TypeStatatementSupport
    //        The idea is to replace wiring around getSupportSpecificForArgument() so that it has a QName rather than
    //        a String. That way we can discern type reference it is:
    //          - static (e.g. 'type uint32')
    //          - dynamic (e.g. 'type decimal64')
    //          - typedef namespace (e.g. 'type foo', 'type foo:bar')
    //        This way we can create check the target exists and a FULL_DECLARATION dependency as part of
    //        onStatementAdded() -- and have it change return type from void to 'StatementSupport<A, D, E>'.
    // TYPE_LINKAGE,

    // FIXME: YANGTOOLS-1762: this phase should add DefaultStatementSupport
    //        The idea is that by the time parseArgument() is called we know everything else, most notably all sibling
    //        'type' statements have been linked and we can therefore parse the argument onto its correct type, e.g.
    //        uint32, instance-identifier, uinion, etc.

    // FIXME: add documentation
    FULL_DECLARATION(STATEMENT_DEFINITION, ExecutionOrder.FULL_DECLARATION),
    // FIXME: add documentation
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
         * Corresponds to {@link ModelProcessingPhase#SOURCE_LINKAGE}.
         */
        public static final byte SOURCE_LINKAGE       = 2;
        /**
         * Corresponds to {@link ModelProcessingPhase#STATEMENT_DEFINITION}.
         */
        public static final byte STATEMENT_DEFINITION = 3;
        /**
         * Corresponds to {@link ModelProcessingPhase#FULL_DECLARATION}.
         */
        public static final byte FULL_DECLARATION     = 4;
        /**
         * Corresponds to {@link ModelProcessingPhase#EFFECTIVE_MODEL}.
         */
        public static final byte EFFECTIVE_MODEL      = 5;

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
        final var values = values();
        final var tmp = new ModelProcessingPhase[values.length + 1];

        for (var phase : values) {
            final byte offset = phase.executionOrder;
            verify(offset > 0, "Invalid execution order in %s", phase);

            final var existing = tmp[offset];
            verify(existing == null, "Execution order %s clash with %s", offset, existing);
            verify(tmp[offset - 1] == phase.previousPhase, "Illegal previous phase of %s", phase);
            tmp[offset] = phase;
        }

        BY_EXECUTION_ORDER = tmp;
    }

    private final @Nullable ModelProcessingPhase previousPhase;
    private final byte executionOrder;

    // For INIT only
    ModelProcessingPhase() {
        previousPhase = null;
        executionOrder = ExecutionOrder.INIT;
    }

    @NonNullByDefault
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
