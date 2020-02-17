/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;

/**
 * Thrown where incorrect nesting of data structures was detected and was caused by user. This typically indicates
 * that class structure encountered could not have possibly been constructed according to the rules understood by the
 * codec.
 */
@Beta
public class IncorrectNestingException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public IncorrectNestingException(final String message) {
        super(message);
    }

    public static IncorrectNestingException create(final String message, final Object... args) {
        return new IncorrectNestingException(String.format(message, args));
    }
}
