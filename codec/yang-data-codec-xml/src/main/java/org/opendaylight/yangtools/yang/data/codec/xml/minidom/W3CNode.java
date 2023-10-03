/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static com.google.common.base.Verify.verifyNotNull;

sealed interface W3CNode extends Node permits W3CAttribute, W3CElement {
    @Override
    default String namespace() {
        return node().getNamespaceURI();
    }

    @Override
    default String localName() {
        return verifyNotNull(node().getLocalName());
    }

    org.w3c.dom.Node node();
}
