/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;


@NonNullByDefault
record W3CContainerElement(org.w3c.dom.Element node) implements ContainerElement, W3CElement {
    W3CContainerElement {
        requireNonNull(node);
    }

    @Override
    public List<Element> children() {
        return new W3CElementList(node.getElementsByTagNameNS("*", "*"));
    }
}
