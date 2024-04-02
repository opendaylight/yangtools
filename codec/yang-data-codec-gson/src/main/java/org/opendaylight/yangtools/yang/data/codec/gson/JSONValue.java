/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A serialized JSON string, indicating what kind of value it represents.
 *
 * @param rawString unescaped string
 * @param kind string kind
 */
@NonNullByDefault
public record JSONValue(String rawString, Kind kind) {
    /**
     * The kind of a {@link JSONValue}. Indicates the semantics of {@link JSONValue#rawString()}.
     */
    public enum Kind {
        /**
         * A {@code boolean} value.
         */
        BOOLEAN,
        /**
         * An {@code empty} value.
         */
        EMPTY,
        /**
         * A numeric value, excluding {@code int64} and {@code uint64)}.
         */
        NUMBER,
        /**
         * A string value.
         */
        STRING
    }

    /**
     * The equivalent on {@link Boolean#FALSE}.
     */
    public static final JSONValue FALSE = new JSONValue("false", Kind.BOOLEAN);
    /**
     * The equivalent on {@link Boolean#TRUE}.
     */
    public static final JSONValue TRUE = new JSONValue("true", Kind.BOOLEAN);
    /**
     * The equivalent on {@link org.opendaylight.yangtools.yang.common.Empty#value()}.
     */
    public static final JSONValue EMPTY = new JSONValue("[null]", Kind.EMPTY);

    public JSONValue {
        requireNonNull(rawString);
        requireNonNull(kind);
    }
}
