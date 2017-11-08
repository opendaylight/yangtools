/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;

/**
 * Base exception reported by {@link YangParser}. This is used as an exception of last resort, semantic reporting
 * of individual errors is performed via subclasses, like {@link YangSyntaxErrorException}.
 *
 * @author Robert Varga
 */
@Beta
public class YangParserException extends Exception {
    private static final long serialVersionUID = 1L;

    public YangParserException(final String message) {
        super(requireNonNull(message));
    }

    public YangParserException(final String message, final Throwable cause) {
        super(requireNonNull(message), cause);
    }
}
