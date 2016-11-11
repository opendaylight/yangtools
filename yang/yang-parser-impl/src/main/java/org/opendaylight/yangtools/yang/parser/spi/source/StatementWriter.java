/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
     * If statement has substatements, in order to start substatement, call to
     * {@link #startStatement(int, QName, String, StatementSourceReference)} needs to be done
     * for substatement.
     *
     * @param childId
     *            Child identifier, unique among siblings
     *
     * @param name
     *            Fully qualified name of statement.
     * @param argument
     *            String representation of value as appeared in source, null if not present
     * @param ref
     *            Identifier of location in source, which will be used for
     *            reporting in case of statement processing error.
     * @throws SourceException
     *             if statement is not valid according to current context.
     */
    void startStatement(final int childId, @Nonnull QName name, @Nullable String argument,
            @Nonnull StatementSourceReference ref);

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
    @Nonnull ModelProcessingPhase getPhase();
}
