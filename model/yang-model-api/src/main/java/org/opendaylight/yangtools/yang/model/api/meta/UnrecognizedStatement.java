/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Common interface for unrecognized statements. An {@link UnrecognizedStatement} is an instance of a statement
 * defined via an {@code extension} statement, for which the parser did not have semantic support.
 *
 * <p>The effective counterpart to this statement is {@link UnrecognizedEffectiveStatement}, which can never be
 * instantiated.
 */
public interface UnrecognizedStatement extends DeclaredStatement<Object> {
    /**
     * {@inheritDoc}
     *
     * <p>This method can return
     * <ul>
     *   <li>{@link String} if the statement has an argument</li>
     *   <li>{@link Empty} if the statement does not have an argument</li>
     * </ul>
     */
    @Override
    default @NonNull Object argument() {
        final var rawArgument = rawArgument();
        return rawArgument != null ? rawArgument : Empty.value();
    }
}
