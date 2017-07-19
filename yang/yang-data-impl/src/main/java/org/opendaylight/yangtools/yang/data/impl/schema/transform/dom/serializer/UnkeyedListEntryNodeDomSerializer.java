/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Deprecated
final class UnkeyedListEntryNodeDomSerializer extends ListEntryNodeDomSerializer<UnkeyedListEntryNode> {

    UnkeyedListEntryNodeDomSerializer(final Document doc, final NodeSerializerDispatcher<Element> dispatcher) {
        super(doc, dispatcher);
    }

}
