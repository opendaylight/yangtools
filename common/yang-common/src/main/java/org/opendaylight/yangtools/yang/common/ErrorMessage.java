/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The contents of a {@code error-message} element as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-7.1">RFC8040 Error Response Message</a>.
 *
 * @param elementBody the string to be displayed
 * @param xmlLang optional Language Identification string
 * @since 15.0.0
 */
@NonNullByDefault
public record ErrorMessage(String elementBody, @Nullable String xmlLang) implements Serializable {
    /**
     * Default constructor.
     *
     * @param elementBody the string to be displayed
     * @param xmlLang optional Language Identification string
     */
    public ErrorMessage {
        requireNonNull(elementBody);
    }

    /**
     * Convenience constructor equivalent to {@code ErrorMessage(elementBody, null)}.
     *
     * @param elementBody the string to be displayed
     */
    public ErrorMessage(final String elementBody) {
        this(elementBody, null);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("elementBody", elementBody)
            .add("xmlLang", xmlLang)
            .toString();
    }

    @java.io.Serial
    private Object writeReplace() {
        return new EMv1(elementBody, xmlLang);
    }
}
