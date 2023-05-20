/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Item ordering, as specified by
 * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-7.7.1">RFC7950 section 7.7.1</a>.
 */
@NonNullByDefault
public enum Ordering {
    /**
     * The equivalent of {@code ordered-by system}.
     */
    SYSTEM("system"),
    /**
     * The equivalent of {@code ordered-by user}.
     */
    USER("user");

    private String argumentString;

    Ordering(final String argumentString) {
        this.argumentString = argumentString;
    }

    /**
     * Return the {code ordered-by} string argument this Ordering represents.
     *
     * @return Argument string
     */
    public String argument() {
        return argumentString;
    }

    /**
     * Return the Ordering corresponding to an argument string.
     *
     * @param argumentString Argument string
     * @return Corresponding Ordering
     * @throws NullPointerException if {code argumentString} is null
     * @throws IllegalArgumentException if the argument string is not a valid Ordering
     */
    public static Ordering forArgument(final String argumentString) {
        return switch (argumentString) {
            case "system" -> SYSTEM;
            case "user" -> USER;
            default -> throw new IllegalArgumentException("Invalid ordering string '" + argumentString + "'");
        };
    }
}