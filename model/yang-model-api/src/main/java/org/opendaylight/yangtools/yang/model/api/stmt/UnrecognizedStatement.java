/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Common interface for unrecognized unknown statements. An {@link UnrecognizedStatement} is an instance of a statement
 * defined via an {@code extension} statement, for which the parser did not have semantic support (in which case the
 * statement would result in a corresponding semantic subclass of {@link UnknownStatement}).
 *
 * <p>
 * This construct does not have a {@link UnknownEffectiveStatement} counterpart because we cannot reasonably build an
 * effective model of something we do not recognize.
 */
public interface UnrecognizedStatement extends UnknownStatement<Object> {
    /**
     * {@inheritDoc}
     *
     * <p>
     * This method can return
     * <ul>
     *   <li>{@link String} if the statement has an argument</li>
     *   <li>{@link Empty} if the statement does not have an argument</li>
     * </ul>
     */
    @Override
    Object argument();
}
