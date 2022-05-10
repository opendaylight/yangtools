/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
 * Enumeration describing {@code deviate}
 * <a href="https://www.rfc-editor.org/rfc/rfc7950.html#section-7.20.3.2">YANG statement</a> argument. It defines how
 * the server implementation of the target node deviates from its original definition.
 */
@NonNullByDefault
public enum DeviateKind {
    /**
     * Target node is not implemented by the server.
     */
    NOT_SUPPORTED("not-supported"),
    /**
     * Server implements target node with additional properties.
     */
    ADD("add"),
    /**
     * Server implements target node with different properties.
     */
    REPLACE("replace"),
    /**
     * Server implements target node without some properties.
     */
    DELETE("delete");

    private final String argument;

    DeviateKind(final String argumentValue) {
        argument = requireNonNull(argumentValue);
    }

    /**
     * Returns the YANG {@code deviate} statement argument value corresponding to this object.
     *
     * @return String that corresponds to the YANG {@code deviate} statement argument
     */
    public String argument() {
        return argument;
    }

    /**
     * Returns the YANG keyword corresponding to this object.
     *
     * @return String that corresponds to the YANG keyword.
     * @deprecated Use {@link #argument} instead.
     */
    @Deprecated(since = "9.0.0", forRemoval = true)
    public String getKeyword() {
        return argument;
    }

    /**
     * Return a {@link DeviateKind} for specified {@code deviate} statement argument. This methods returns a
     * {@code null} for illegal values. See {@link #ofArgument(String)} for a version which returns non-null and throws
     * an exception for illegal values.
     *
     * @param argument {@code deviate} statement argument
     * @return An enumeration value, or {@code null} if specified argument is not valid
     * @throws NullPointerException if {@code argument} is {@code null}
     */
    public static @Nullable DeviateKind forArgument(final String argument) {
        return switch (argument) {
            case "not-supported" -> NOT_SUPPORTED;
            case "add" -> ADD;
            case "replace" -> REPLACE;
            case "delete" -> DELETE;
            default -> null;
        };
    }

    /**
     * Return a {@link DeviateKind} for specified {@code deviate} statement argument. This methods throws an exception
     * for illegal values. See {@link #forArgument(String)} for a version which returns a {@code null} instead for
     * illegal values.
     *
     * @param argument {@code deviate} statement argument
     * @return An enumeration value
     * @throws NullPointerException if {@code argument} is {@code null}
     * @throws IllegalArgumentException if {@code argument} is not a valid {@code deviate} statement argument
     */
    public static DeviateKind ofArgument(final String argument) {
        final var ret = forArgument(argument);
        if (ret == null) {
            throw new IllegalArgumentException("\"" + argument + "\" is not a valid deviate statement argument");
        }
        return ret;
    }
}
