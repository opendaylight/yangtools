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
 * Enumeration of {@code error-type} values. These provide glue between {@link NetconfLayer} and various sources of
 * such errors. This enumeration is not extensible in YANG as it is modeled in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-3.9">RFC8040</a>.
 */
@NonNullByDefault
public enum ErrorType {
    /**
     * A {@link NetconfLayer#TRANSPORT} layer error. This typically happens on transport endpoints, where a protocol
     * plugin needs to report a NETCONF-equivalent condition.
     */
    TRANSPORT("transport", NetconfLayer.TRANSPORT),
    /**
     * A {@link NetconfLayer#RPC} layer error. This typically happens on request routers, where a request may end up
     * being resolved due to implementation-internal causes, such as timeouts and state loss.
     */
    RPC("rpc", NetconfLayer.RPC),
    /**
     * A {@link NetconfLayer#OPERATIONS} layer error. These typically happen in a NETCONF protocol implementation.
     */
    PROTOCOL("protocol", NetconfLayer.OPERATIONS),
    /**
     * A {@link NetconfLayer#CONTENT} layer error. These typically happen due to YANG data handling, such as
     * type checking and structural consistency.
     */
    APPLICATION("application", NetconfLayer.CONTENT);

    private static final Map<String, ErrorType> BY_ELEMENT_BODY =
        Maps.uniqueIndex(Arrays.asList(values()), ErrorType::elementBody);

    private final String elementBody;
    private final NetconfLayer layer;

    ErrorType(final String elementName, final NetconfLayer layer) {
        this.elementBody = requireNonNull(elementName);
        this.layer = requireNonNull(layer);
    }

    /**
     * Return the XML element body of this object.
     *
     * @return element body of this object
     */
    public String elementBody() {
        return elementBody;
    }

    /**
     * Return the {@link NetconfLayer} corresponding to this error type.
     *
     * @return A NETCONF layer
     */
    public final NetconfLayer layer() {
        return layer;
    }

    public static @Nullable ErrorType forElementBody(final String elementBody) {
        return BY_ELEMENT_BODY.get(requireNonNull(elementBody));
    }
}