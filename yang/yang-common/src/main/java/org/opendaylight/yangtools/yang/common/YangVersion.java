/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Enumeration of supported YANG versions.
 *
 * @author Robert Varga
 */
@Beta
public enum YangVersion {
    /**
     * Version 1, as defined in RFC6020.
     */
    VERSION_1("1", "RFC6020"),
    /**
     * Version 1.1, as defined in RFC7950.
     */
    VERSION_1_1("1.1", "RFC7950");

    private static final Map<String, YangVersion> YANG_VERSION_MAP = Maps.uniqueIndex(Arrays.asList(values()),
        YangVersion::toString);

    private final String str;
    private String reference;

    YangVersion(final String str, final String reference) {
        this.str = requireNonNull(str);
        this.reference = requireNonNull(reference);
    }

    /**
     * Parse a YANG version from its textual representation.
     *
     * @param str String to parse
     * @return YANG version
     * @throws NullPointerException if the string is null
     */
    public static Optional<YangVersion> parse(@Nonnull final String str) {
        return Optional.ofNullable(YANG_VERSION_MAP.get(requireNonNull(str)));
    }

    /**
     * Return the normative reference defining this YANG version.
     *
     * @return Normative reference.
     */
    @Nonnull public String getReference() {
        return reference;
    }

    @Override
    @Nonnull public String toString() {
        return str;
    }
}
