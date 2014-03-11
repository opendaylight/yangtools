/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;

import java.util.List;

public interface NodeParserDispatcher<E> {

    DataContainerChild<?, ?> dispatchChildElement(Object schema, List<E> childNodes);

    public static abstract class BaseNodeParserDispatcher<E> implements NodeParserDispatcher<E> {

        @Override
        public DataContainerChild<?, ?> dispatchChildElement(Object schema, List<E> childNodes) {
            Preconditions.checkArgument(childNodes.isEmpty() == false);

            if (schema instanceof ContainerSchemaNode) {
                return getContainerNodeDomParser().parse(childNodes, (ContainerSchemaNode) schema);
            } else if (schema instanceof LeafSchemaNode) {
                return getLeafNodeDomParser().parse(childNodes, (LeafSchemaNode) schema);
            } else if (schema instanceof LeafListSchemaNode) {
                return getLeafSetNodeDomParser().parse(childNodes, (LeafListSchemaNode) schema);
            } else if (schema instanceof ListSchemaNode) {
                return getMapNodeDomParser().parse(childNodes, (ListSchemaNode) schema);
            } else if (schema instanceof ChoiceNode) {
                return getChoiceNodeDomParser().parse(childNodes, (ChoiceNode) schema);
            } else if (schema instanceof AugmentationSchema) {
                return getAugmentationNodeDomParser().parse(childNodes, (AugmentationSchema) schema);
            }

            throw new IllegalArgumentException("Unable to parse node, unknown schema type: " + schema);
        }

        protected abstract ToNormalizedNodeParser<E, ContainerNode, ContainerSchemaNode> getContainerNodeDomParser();

        protected abstract ToNormalizedNodeParser<E, LeafNode<?>, LeafSchemaNode> getLeafNodeDomParser();

        protected abstract ToNormalizedNodeParser<E, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeDomParser();

        protected abstract ToNormalizedNodeParser<E, MapNode, ListSchemaNode> getMapNodeDomParser();

        protected abstract ToNormalizedNodeParser<E, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode, ChoiceNode> getChoiceNodeDomParser();

        protected abstract ToNormalizedNodeParser<E, AugmentationNode, AugmentationSchema> getAugmentationNodeDomParser();

    }
}
