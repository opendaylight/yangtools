/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enumeration of {@code error-severity} values, as defined by
 * <a href="https://www.rfc-editor.org/rfc/rfc4741#section-4.3">RFC4741, section 4.3</a>.
 */
@NonNullByDefault
public enum ErrorSeverity {
    /**
     * An error preventing an operation from completing successfully.
     */
    ERROR("error"),
    /**
     * A warning not affecting an operation's ability to complete successfully.
     */
    WARNING("warning");

    private static final Map<String, ErrorSeverity> BY_ELEMENT_BODY =
        Maps.uniqueIndex(Arrays.asList(values()), ErrorSeverity::elementBody);

    private final String elementBody;

    ErrorSeverity(final String elementName) {
        this.elementBody = requireNonNull(elementName);
    }

    /**
     * Return the XML element body of this object.
     *
     * @return element body of this object
     */
    public String elementBody() {
        return elementBody;
    }

    public static @Nullable ErrorSeverity forElementBody(final String elementBody) {
        return BY_ELEMENT_BODY.get(requireNonNull(elementBody));
    }
}
