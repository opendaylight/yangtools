/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Maximum allowed access, as defined by
 * <a href="https://tools.ietf.org/html/rfc2578#section-7.3">RFC2578 Section 7.3</a>.
 */
@Beta
public enum MaxAccess {
    /**
     * Indicates the annotated object is an auxiliary object, as per
     * <a href="https://tools.ietf.org/html/rfc2578#section-7.7">RFC2578 Section 7.7</a>.
     */
    NOT_ACCESSIBLE("not-accessible"),
    /**
     * Indicates the annotated object is accessible only for notifications.
     */
    ACCESSIBLE_FOR_NOTIFY("accessible-for-notify"),
    /**
     * Indicates that {@code read} access makes 'protocol sense', but  {@code write} and {@code create} do not.
     */
    READ_ONLY("read-only"),
    /**
     * Indicates that {@code read} and {@code write} access make 'protocol sense', but {@code create} does not.
     */
    READ_WRITE("read-write"),
    /**
     * Indicates that {@code read}, {@code write} and {@code create} access make 'protocol sense'.
     */
    READ_CREATE("read-create");

    private static final ImmutableMap<String, MaxAccess> VALUES =
            Maps.uniqueIndex(Arrays.asList(MaxAccess.values()), MaxAccess::stringLiteral);

    private @NonNull String str;

    MaxAccess(final @NonNull String str) {
        this.str = str;
    }

    public @NonNull String stringLiteral() {
        return str;
    }

    public static @Nullable MaxAccess forStringLiteral(final @NonNull String str) {
        return VALUES.get(requireNonNull(str));
    }
}
