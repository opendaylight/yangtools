/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.databind;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The contents of a {@code error-info} element as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-7.1">RFC8040 Error Response Message</a>. This interface
 * anchors a sealed class hierarchy of all known specializations.
 *
 * @since 15.0.0
 */
// FIXME: This really should be a FormattableBody or similar, i.e. structured content which itself is formattable --
//        unlike FormattableBody, though, it needs to be defined as being formatted to an output. That opens a can of
//        inter-econding compatibility issues exactly like the ones we encounter with anydata.
// FIXME: given that the normalized-node-based FormattableBody lives far away in restconf-server-spi, we will need some
//        sort of a solution here.
//
// FIXME: the solution to both concerns are okay, but we really do not have to do much here:
//
//          - we need to define a new kind of DatabindPath and carry it here
//          - we will carry a NormalizedNode
//
//        Encodings can then easily be dealt with to the point NormalizedNode allows using normal Databind encoding
//        operation.
@Beta
@NonNullByDefault
public sealed interface ErrorInfo {
    /**
     * Legacy model, inherited from original design of {@code RpcError} and {@code RestconfError}. This is the
     * equivalent of having {@code error-info} resolve to {@code leaf error-info { type string; }}.
     *
     * @param elementBody the elementBody
     * @since 15.0.0
     */
    @Beta
    record OfLiteral(String elementBody) implements ErrorInfo {
        /**
         * Default constructor.
         *
         * @param elementBody the elementBody
         */
        public OfLiteral {
            requireNonNull(elementBody);
        }
    }
}
