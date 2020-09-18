/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Maximum allowed access, as defined by <a href="https://tools.ietf.org/html/rfc2578#section-7.3">RFC2578</a>.
 */
@Beta
public enum MaxAccess {
    NOT_ACCESSIBLE("not-accessible"),
    ACCESSIBLE_FOR_NOTIFY("accessible-for-notify"),
    READ_ONLY("read-only"),
    READ_WRITE("read-write"),
    READ_CREATE("read-create");

    private static final ImmutableMap<String, MaxAccess> VALUES =
            Maps.uniqueIndex(Arrays.asList(MaxAccess.values()), MaxAccess::stringLiteral);

    private @NonNull String str;

    private MaxAccess(final @NonNull String str) {
        this.str = str;
    }

    public @NonNull String stringLiteral() {
        return str;
    }

    public static @NonNull MaxAccess forStringLiteral(final @NonNull String str) {
        final MaxAccess value = VALUES.get(requireNonNull(str));
        checkArgument(value != null, "Unknown literal value '%s'", str);
        return value;
    }
}
