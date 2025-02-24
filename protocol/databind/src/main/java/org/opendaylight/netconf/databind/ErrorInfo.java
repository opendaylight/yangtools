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

/**
 * The contents of a {@code error-info} element as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-7.1">RFC8040 Error Response Message</a>, bound to a
 * {@link DatabindContext}.
 *
 * @param elementBody the elementBody
 */
// FIXME: String here is legacy coming from RestconfError. This really should be a FormattableBody or similar, i.e.
//        structured content which itself is formattable -- unlike FormattableBody, though, it needs to be defined as
//        being formatted to a output. This format should include writing.
// FIXME: given that the normalized-node-based FormattableBody lives in server.spi, this should probably be an interface
//        implemented in at server.spi level.
@Beta
public record ErrorInfo(String elementBody) {
    public ErrorInfo {
        requireNonNull(elementBody);
    }
}
