/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.databind;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The contents of a {@code error-message} element as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-7.1">RFC8040 Error Response Message</a>, bound to a
 * {@link DatabindContext}.
 *
 * @param elementBody the string to be displayed
 * @param xmlLang optional Language Identification string
 */
// FIXME: move to yangtools.yang.common for integration into RpcError
@Beta
@NonNullByDefault
public record ErrorMessage(String elementBody, @Nullable String xmlLang) {
    public ErrorMessage {
        requireNonNull(elementBody);
    }

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
}