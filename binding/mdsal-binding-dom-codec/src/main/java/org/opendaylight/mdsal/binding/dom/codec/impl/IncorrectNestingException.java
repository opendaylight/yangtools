/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Thrown where incorrect nesting of data structures was detected
 * and was caused by user.
 */
public class IncorrectNestingException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    protected IncorrectNestingException(final String message) {
        super(message);
    }

    public static IncorrectNestingException create(final String message, final Object... args) {
        return new IncorrectNestingException(String.format(message, args));
    }

    public static void check(final boolean check, final String message, final Object... args) {
        if(!check) {
            throw IncorrectNestingException.create(message, args);
        }
    }

    @Nonnull
    public static <V> V checkNonNull(@Nullable final V nullable, final String message, final Object... args) {
        if(nullable != null) {
            return nullable;
        }
        throw IncorrectNestingException.create(message, args);
    }
}
