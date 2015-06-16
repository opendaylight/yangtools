/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

public interface StatementWriter {

    /**
     *
     * Starts statement with supplied name and location in source.
     *
     *
     * <p>
     * Each started statement must also be closed by
     * {@link #endStatement(StatementSourceReference)} in order for stream to be
     * correct.
     * </p>
     * <p>
     * If statement requires an argument, user must call
     * {@link #argumentValue(String, StatementSourceReference)} to emit
     * argument, otherwise any subsequent call to this {@link StatementWriter}
     * will throw {@link SourceException}.
     * </p>
     * <p>
     * If statement has substatements, in order to start substatement, call to
     * {@link #startStatement(QName, StatementSourceReference)} needs to be done
     * for substatement.
     *
     * @param name
     *            Fully qualified name of statement.
     * @param ref
     *            Identifier of location in source, which will be used for
     *            reporting in case of statement processing error.
     * @throws SourceException
     *             if statement is not valid according to current context.
     */
    void startStatement(@Nonnull QName name, @Nonnull StatementSourceReference ref) throws SourceException;

    /**
     *
     * Emits an argument to current opened statement.
     *
     * <p>
     * If statement has an argument, this must be called right after
     * {@link #startStatement(QName, StatementSourceReference)} otherwise any
     * subsequent call to this {@link StatementWriter} will throw
     * {@link SourceException}.
     *
     *
     *
     * @param value
     *            String representation of value as appeared in source
     * @param ref
     *            Identifier of location in source, which will be used for
     *            reporting in case of statement processing error.
     * @throws SourceException
     *             if argument is not valid for current statement.
     */
    void argumentValue(@Nonnull String value,@Nonnull StatementSourceReference ref) throws SourceException;

    /**
     * Ends current opened statement.
     *
     * @param ref
     *            Identifier of location in source, which will be used for
     *            reporting in case of statement processing error.
     * @throws SourceException
     *             if closed statement is not valid in current context, or there
     *             is no such statement
     */
    void endStatement(@Nonnull StatementSourceReference ref) throws SourceException;

    /**
     *
     * @return current processing phase
     */
    @Nonnull
    ModelProcessingPhase getPhase();
}
