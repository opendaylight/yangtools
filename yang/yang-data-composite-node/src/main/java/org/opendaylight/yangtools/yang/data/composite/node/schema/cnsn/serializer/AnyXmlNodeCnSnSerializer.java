/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.serializer;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.AnyXmlNodeBaseSerializer;

public class AnyXmlNodeCnSnSerializer extends AnyXmlNodeBaseSerializer<Node<?>> {

    @Override
    protected Node<?> serializeAnyXml(AnyXmlNode node) {
        // This should be removed along with composite nodes
        return null;
    }
}
