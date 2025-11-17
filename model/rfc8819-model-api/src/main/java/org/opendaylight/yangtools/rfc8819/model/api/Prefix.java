/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.eclipse.jdt.annotation.NonNull;

/**
 * YANG Module Tag Prefixes Registry, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8819#section-7.1">RFC8819</a>.
 *
 * <p>This registry allocates tag prefixes. All YANG module tags SHOULD begin with one of the prefixes in this registry.
 * Prefix entries in this registry should be short strings consisting of lowercase ASCII alpha-numeric characters and
 * a final ":" character.
 *
 * @param value the prefix value
 */
public record Prefix(@NonNull String value) {
    private static final Interner<@NonNull Prefix> INTERNER = Interners.newWeakInterner();

    /**
     * {@code ietf:} {@link Prefix}. Covers mechanics specified in RFC8819 section 7.1, table 1
     * <pre>
     *    IETF tags allocated in the IANA "IETF YANG Module Tags" registry.
     * </pre>
     */
    public static final @NonNull Prefix IETF = new Prefix("ietf:").intern();
    /**
     * {@code vendor:} {@link Prefix}. Covers mechanics specified in RFC8819 section 7.1, table 1
     * <pre>
     *     Non-registered tags allocated by the module implementer.
     * </pre>
     */
    public static final @NonNull Prefix VENDOR = new Prefix("vendor:").intern();
    /**
     * {@code user:} {@link Prefix}. Covers mechanics specified in RFC8819 section 7.1, table 1
     * <pre>
     *     Non-registered tags allocated by and for the user.
     * </pre>
     */
    public static final @NonNull Prefix USER = new Prefix("user:").intern();

    /**
     * Default constructor.
     *
     * @param value the prefix value
     */
    public Prefix {
        requireNonNull(value);
        checkArgument(Tag.isValidValue(value), "Invalid prefix value '%s'.", value);
    }

    /**
     * {@return an interned equivalent of this prefix}
     */
    public @NonNull Prefix intern() {
        return INTERNER.intern(this);
    }
}
