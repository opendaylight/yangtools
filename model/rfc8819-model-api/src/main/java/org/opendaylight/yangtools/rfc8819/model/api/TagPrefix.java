/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;

/**
 * YANG Module Tag Prefixes Registry, as defined in
 * <a href="https://datatracker.ietf.org/doc/html/rfc8819#section-7.1">RFC8819</a>.
 *
 * <p>
 * This registry allocates tag prefixes.  All YANG module tags SHOULD
 * begin with one of the prefixes in this registry.
 * Prefix entries in this registry should be short strings consisting of
 * lowercase ASCII alpha-numeric characters and a final ":" character.
 * </p>
 */
public final class TagPrefix {
    /**
     * {@code ietf:} {@link TagPrefix}. Covers mechanics specified in RFC8819 section 7.1, table 1
     * <pre>
     *    IETF tags allocated in the IANA "IETF YANG Module Tags" registry.
     * </pre>
     */
    public static final @NonNull TagPrefix IETF = new TagPrefix("ietf:");
    /**
     * {@code vendor:} {@link TagPrefix}. Covers mechanics specified in RFC8819 section 7.1, table 1
     * <pre>
     *     Non-registered tags allocated by the module implementer.
     * </pre>
     */
    public static final @NonNull TagPrefix VENDOR = new TagPrefix("vendor:");
    /**
     * {@code user:} {@link TagPrefix}. Covers mechanics specified in RFC8819 section 7.1, table 1
     * <pre>
     *     Non-registered tags allocated by and for the user.
     * </pre>
     */
    public static final @NonNull TagPrefix USER = new TagPrefix("user:");

    private final @NonNull String prefix;

    TagPrefix(final String prefix) {
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
