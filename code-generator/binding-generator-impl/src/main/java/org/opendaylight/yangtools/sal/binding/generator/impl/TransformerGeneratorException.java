/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

final class TransformerGeneratorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TransformerGeneratorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static TransformerGeneratorException wrap(final Class<?> cls, final Throwable cause) throws TransformerGeneratorException {
        return new TransformerGeneratorException(String.format("Failed to generate for type %s", cls), cause);
    }
}
