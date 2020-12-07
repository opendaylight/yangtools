/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

public interface StatementWriter {
    /**
     * Resumed statement state.
     *
     * @author Robert Varga
     */
    @Beta
    @NonNullByDefault
    interface ResumedStatement {
        /**
         * Return statement definition.
         *
         * @return statement definition.
         */
        StatementDefinition getDefinition();

        /**
         * Return statement source reference.
         *
         * @return statement source reference.
         */
        StatementSourceReference getSourceReference();

        /**
         * Check if the statement has been fully defined. This implies that all its children have been fully defined.
         *
         * @return True if the statement has been fully defined.
         */
        boolean isFullyDefined();
    }

    /**
     * Attempt to resume a child statement. If the statement has been previously defined, a {@link ResumedStatement}
     * instance is returned.
     *
     * <p>
     * If an empty optional is returned, the caller is expected to follow-up with
     * {@link #startStatement(int, QName, String, StatementSourceReference)} to define the statement.
     *
     * <p>
     * If the returned resumed statement indicates {@link ResumedStatement#isFullyDefined()}, the caller should take
     * no further action with this or any of the child statements. Otherwise this call is equivalent of issuing
     * {@link #startStatement(int, QName, String, StatementSourceReference)} and the caller is expected to process
     * any child statements. The caller should call {@link #storeStatement(int, boolean)} before finishing processing
     * with {@link #endStatement(StatementSourceReference)}.
     *
     * @param childId Child
     * @return A resumed statement or empty if the statement has not previously been defined.
     */
    Optional<? extends ResumedStatement> resumeStatement(int childId);

    /**
     * Store a defined statement, hinting at the number of children it is expected to have and indicating whether
     * it has been fully defined. This method should be called before {@link #endStatement(StatementSourceReference)}
     * when the caller is taking advantage of {@link #resumeStatement(int)}.
     *
     * @param expectedChildren Number of expected children, cannot be negative
     * @param fullyDefined True if the statement and all its descendants have been defined.
     */
    void storeStatement(int expectedChildren, boolean fullyDefined);

    /**
     * Starts statement with supplied name and location in source.
     *
     * <p>
     * Each started statement must also be closed by
     * {@link #endStatement(StatementSourceReference)} in order for stream to be
     * correct.
     *
     * <p>
     * If statement has substatements, in order to start substatement, call to
     * {@link #startStatement(int, QName, String, StatementSourceReference)} needs to be done for substatement.
     *
     * @param childId Child identifier, unique among siblings
     * @param name Fully qualified name of statement.
     * @param argument String representation of value as appeared in source, null if not present
     * @param ref Identifier of location in source, which will be used for reporting in case of statement processing
     *            error.
     * @throws SourceException if statement is not valid according to current context.
     */
    void startStatement(int childId, @NonNull QName name, @Nullable String argument,
            @NonNull StatementSourceReference ref);

    /**
     * Ends current opened statement.
     *
     * @param ref Identifier of location in source, which will be used for reporting in case of statement processing
     *            error.
     * @throws SourceException if closed statement is not valid in current context, or there is no such statement
     */
    void endStatement(@NonNull StatementSourceReference ref);

    /**
     * Return current model processing phase.
     *
     * @return current processing phase
     */
    @NonNull ModelProcessingPhase getPhase();
}
