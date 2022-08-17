/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.Serial;

@Beta
public class AnydataNormalizationException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public AnydataNormalizationException(final String message) {
        super(requireNonNull(message));
    }

    public AnydataNormalizationException(final String message, final Throwable cause) {
        super(requireNonNull(message), cause);
    }
}
