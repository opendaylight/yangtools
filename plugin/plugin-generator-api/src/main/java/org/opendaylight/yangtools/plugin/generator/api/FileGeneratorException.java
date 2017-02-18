/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;

/**
 * An exception reporting a problem with file generation. This exception should be reported when exception chaining
 * occurs.
 */
@Beta
public class FileGeneratorException extends Exception {
    private static final long serialVersionUID = 1L;

    public FileGeneratorException(final String message) {
        super(requireNonNull(message));
    }

    public FileGeneratorException(final String message, final Throwable cause) {
        super(requireNonNull(message), cause);
    }
}
