/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enumeration describing YANG 'status' statement. If no status is specified, the
 * default is CURRENT.
 */
@NonNullByDefault
public enum Status {
    /**
     * CURRENT means that the definition is current and valid.
     */
    CURRENT("current"),
    /**
     * DEPRECATED indicates an obsolete definition, but it permits new/
     * continued implementation in order to foster interoperability with
     * older/existing implementations.
     */
    DEPRECATED("deprecated"),
    /**
     * OBSOLETE means the definition is obsolete and SHOULD NOT be implemented
     * and/or can be removed from implementations.
     */
    OBSOLETE("obsolete");

    private final String argument;

    Status(final String argument) {
        this.argument = requireNonNull(argument);
    }

    /**
     * Returns the YANG {@code status} statement argument value corresponding to this object.
     *
     * @return String that corresponds to the YANG {@code status} statement argument
     */
    public String argument() {
        return argument;
    }

    /**
     * Return a {@link Status} for specified {@code status} statement argument. This methods returns a  {@code null} for
     * illegal values. See {@link #ofArgument(String)} for a version which returns non-null and throws an exception for
     * illegal values.
     *
     * @param argument {@code status} statement argument
     * @return An enumeration value, or {@code null} if specified argument is not valid
     * @throws NullPointerException if {@code argument} is {@code null}
     */
    public static @Nullable Status forArgument(final String argument) {
        return switch (argument) {
            case "current" -> CURRENT;
            case "deprecated" -> DEPRECATED;
            case "obsolete" -> OBSOLETE;
            default -> null;
        };
    }

    /**
     * Return a {@link Status} for specified {@code status} statement argument. This methods throws an exception for
     * illegal values. See {@link #forArgument(String)} for a version which returns a {@code null} instead for illegal
     * values.
     *
     * @param argument {@code status} statement argument
     * @return An enumeration value, or {@code null} if specified argument is not valid
     * @throws NullPointerException if {@code argument} is {@code null}
     * @throws IllegalArgumentException if {@code argument} is not a valid {@code status} statement argument
     */
    public static Status ofArgument(final String argument) {
        return switch (argument) {
            case "current" -> CURRENT;
            case "deprecated" -> DEPRECATED;
            case "obsolete" -> OBSOLETE;
            default -> throw new IllegalArgumentException(
                "\"" + argument + "\" is not a valid status statement argument");
        };
    }
}
