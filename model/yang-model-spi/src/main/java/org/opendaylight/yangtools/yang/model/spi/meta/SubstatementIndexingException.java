/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;

/**
 * Exception thrown when indexing of substatements of either a {@link AbstractDeclaredEffectiveStatement} or
 * {@link AbstractUndeclaredEffectiveStatement} fails.
 */
@Beta
public final class SubstatementIndexingException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public SubstatementIndexingException(final String message) {
        super(requireNonNull(message));
    }
}
