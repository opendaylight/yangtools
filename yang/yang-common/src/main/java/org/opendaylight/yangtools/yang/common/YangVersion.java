/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
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

    private final String str;
    private String reference;

    private YangVersion(final String str, final String reference) {
        this.str = Preconditions.checkNotNull(str);
        this.reference = Preconditions.checkNotNull(reference);
    }

    /**
     * Parse a YANG version from its textual representation.
     *
     * @param str String to parse
     * @return YANG version
     * @throws IllegalArgumentException if the string is malformed
     * @throws NullPointerException if the string is null
     */
    public static YangVersion parse(@Nonnull final String str) {
        switch (str) {
            case "1":
                return VERSION_1;
            case "1.1":
                return VERSION_1_1;
            default:
                throw new IllegalArgumentException("Invalid YANG version '" + str + "'");
        }
    }

    /**
     * Return the normative reference defining this YANG version.
     *
     * @return Normative reference.
     */
    @Nonnull public String getReference() {
        return reference;
    }

    /**
     * Return the canonical string represetation of this YANG version.
     * @return Canonical string
     */
    @Nonnull public String toCanonicalString() {
        return str;
    }
}
