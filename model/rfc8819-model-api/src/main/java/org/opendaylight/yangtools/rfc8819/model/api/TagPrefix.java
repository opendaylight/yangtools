/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;

public final class TagPrefix {
    public static final @NonNull TagPrefix IETF = new TagPrefix("ietf:");
    public static final @NonNull TagPrefix VENDOR = new TagPrefix("vendor:");
    public static final @NonNull TagPrefix USER = new TagPrefix("user:");

    private final String prefix;

    TagPrefix(String prefix) {
        this.prefix = requireNonNull(prefix);
    }

    public @NonNull String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }
}
