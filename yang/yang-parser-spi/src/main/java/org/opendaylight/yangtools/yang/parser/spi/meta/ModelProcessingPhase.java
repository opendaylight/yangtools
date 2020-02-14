/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
@SuppressFBWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
public enum ModelProcessingPhase {
    INIT(null),

    /**
     * Preliminary cross-source relationship resolution phase which collects available module names and module
     * namespaces. It is necessary in order to correct resolution of unknown statements used in linkage phase (e.g.
     * semantic version of yang modules).
     */
    SOURCE_PRE_LINKAGE(INIT),

    /**
     * Cross-source relationship resolution phase.
     *
     * <p>
     * In this phase of processing only statements which affects cross-source relationship (e.g. imports / includes)
     * are processed.
     *
     * <p>
     * At end of this phase all source related contexts should be bind to their imports and includes to allow
     * visibility of custom defined statements in subsequent phases.
     */
    SOURCE_LINKAGE(SOURCE_PRE_LINKAGE),
    STATEMENT_DEFINITION(SOURCE_LINKAGE),
    FULL_DECLARATION(STATEMENT_DEFINITION),
    EFFECTIVE_MODEL(FULL_DECLARATION);

    private final @Nullable ModelProcessingPhase previousPhase;

    ModelProcessingPhase(final @Nullable ModelProcessingPhase previous) {
        this.previousPhase = previous;
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
}
