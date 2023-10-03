/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.w3c.dom.Attr;

@NonNullByDefault
record W3CAttribute(Attr node) implements W3CNode, Attribute {
    W3CAttribute {
        requireNonNull(node);
    }

    @Override
    public String value() {
        return verifyNotNull(node.getValue());
    }
}
