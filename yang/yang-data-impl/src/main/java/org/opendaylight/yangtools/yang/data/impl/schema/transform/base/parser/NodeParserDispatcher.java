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

/**
 *
 * Dispatches the parsing process of elements according to schema and returns the parsed Node.
 *
 * @param <E> type of elements parsed
 */
public interface NodeParserDispatcher<E> {

    DataContainerChild<?, ?> dispatchChildElement(Object schema, List<E> childNodes);

    /**
     * Abstract implementation that implements the dispatch conditions. Only requires parsers to be provided.
     * The same instance of parser can be provided in case it is immutable.
     */
    public static abstract class BaseNodeParserDispatcher<E> implements NodeParserDispatcher<E> {

        @Override
        public final DataContainerChild<?, ?> dispatchChildElement(Object schema, List<E> childNodes) {
            Preconditions.checkArgument(childNodes.isEmpty() == false);

            if (schema instanceof ContainerSchemaNode) {
                return getContainerNodeParser().parse(childNodes, (ContainerSchemaNode) schema);
            } else if (schema instanceof LeafSchemaNode) {
                return getLeafNodeParser().parse(childNodes, (LeafSchemaNode) schema);
            } else if (schema instanceof LeafListSchemaNode) {
                return getLeafSetNodeParser().parse(childNodes, (LeafListSchemaNode) schema);
            } else if (schema instanceof ListSchemaNode) {
                return getMapNodeParser().parse(childNodes, (ListSchemaNode) schema);
            } else if (schema instanceof ChoiceNode) {
                return getChoiceNodeParser().parse(childNodes, (ChoiceNode) schema);
            } else if (schema instanceof AugmentationSchema) {
                return getAugmentationNodeParser().parse(childNodes, (AugmentationSchema) schema);
            }

            throw new IllegalArgumentException("Unable to parse node, unknown schema type: " + schema);
        }

        protected abstract ToNormalizedNodeParser<E, ContainerNode, ContainerSchemaNode> getContainerNodeParser();

        protected abstract ToNormalizedNodeParser<E, LeafNode<?>, LeafSchemaNode> getLeafNodeParser();

        protected abstract ToNormalizedNodeParser<E, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeParser();

        protected abstract ToNormalizedNodeParser<E, MapNode, ListSchemaNode> getMapNodeParser();

        protected abstract ToNormalizedNodeParser<E, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode, ChoiceNode> getChoiceNodeParser();

        protected abstract ToNormalizedNodeParser<E, AugmentationNode, AugmentationSchema> getAugmentationNodeParser();

    }
}
