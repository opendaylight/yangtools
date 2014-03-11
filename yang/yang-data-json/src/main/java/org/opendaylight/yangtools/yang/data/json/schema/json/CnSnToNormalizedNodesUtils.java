/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.json;

import java.util.Collection;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;

import com.google.common.collect.LinkedListMultimap;

public class CnSnToNormalizedNodesUtils {

    public static LinkedListMultimap<QName, Node<?>> mapChildElementsForSingletonNode(Node<?> node) {
        return mapChildElements( ((CompositeNode)node).getValue());
    }

    public static LinkedListMultimap<QName, Node<?>> mapChildElements(Collection<Node<?>> childNodesCollection) {
        LinkedListMultimap<QName, Node<?>> mappedChildElements = LinkedListMultimap.create();

        for (Node<?> node : childNodesCollection) {
            mappedChildElements.put(node.getNodeType(), node);
        }

        return mappedChildElements;
    }
}
