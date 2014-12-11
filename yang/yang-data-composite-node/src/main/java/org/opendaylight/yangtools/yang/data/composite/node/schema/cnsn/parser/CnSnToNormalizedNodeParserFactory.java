/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.parser;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class CnSnToNormalizedNodeParserFactory implements ToNormalizedNodeParserFactory<Node<?>> {

    private final ContainerNodeCnSnParser containerNodeCnSnParser;
    private final LeafNodeCnSnParser leafNodeCnSnParser;
    private final LeafSetEntryNodeCnSnParser leafSetEntryNodeCnSnParser;
    private final LeafSetNodeCnSnParser leafSetNodeCnSnParser;
    private final MapNodeCnSnParser mapNodeCnSnParser;
    private final MapEntryNodeCnSnParser mapEntryNodeCnSnParser;
    private final ChoiceNodeCnSnParser choiceNodeCnSnParser;
    private final AugmentationNodeCnSnParser augmentationNodeCnSnParser;
    private final AnyXmlNodeCnSnParser anyXmlNodeCnSnParser;

    private CnSnToNormalizedNodeParserFactory() {
        leafNodeCnSnParser = new LeafNodeCnSnParser();
        leafSetEntryNodeCnSnParser = new LeafSetEntryNodeCnSnParser();
        leafSetNodeCnSnParser = new LeafSetNodeCnSnParser(leafSetEntryNodeCnSnParser);
        anyXmlNodeCnSnParser = new AnyXmlNodeCnSnParser();

        final NodeParserDispatcher<Node<?>> dispatcher = new NodeParserDispatcher.BaseNodeParserDispatcher<Node<?>>(
                this) {

        };

        containerNodeCnSnParser = new ContainerNodeCnSnParser(dispatcher);
        mapEntryNodeCnSnParser = new MapEntryNodeCnSnParser(dispatcher);
        mapNodeCnSnParser = new MapNodeCnSnParser(mapEntryNodeCnSnParser);
        choiceNodeCnSnParser = new ChoiceNodeCnSnParser(dispatcher);
        augmentationNodeCnSnParser = new AugmentationNodeCnSnParser(dispatcher);

    }

    public static CnSnToNormalizedNodeParserFactory getInstance() {
        return new CnSnToNormalizedNodeParserFactory();
    }

    @Override
    public ToNormalizedNodeParser<Node<?>, ContainerNode, ContainerSchemaNode> getContainerNodeParser() {
        return containerNodeCnSnParser;
    }

    @Override
    public ToNormalizedNodeParser<Node<?>, LeafNode<?>, LeafSchemaNode> getLeafNodeParser() {
        return leafNodeCnSnParser;
    }

    @Override
    public ToNormalizedNodeParser<Node<?>, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeParser() {
        return leafSetNodeCnSnParser;
    }

    @Override
    public ToNormalizedNodeParser<Node<?>, MapNode, ListSchemaNode> getMapNodeParser() {
        return mapNodeCnSnParser;
    }

    @Override
    public ToNormalizedNodeParser<Node<?>, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode, ChoiceNode> getChoiceNodeParser() {
        return choiceNodeCnSnParser;
    }

    @Override
    public ToNormalizedNodeParser<Node<?>, AugmentationNode, AugmentationSchema> getAugmentationNodeParser() {
        return augmentationNodeCnSnParser;
    }

    @Override
    public ToNormalizedNodeParser<Node<?>, LeafSetEntryNode<?>, LeafListSchemaNode> getLeafSetEntryNodeParser() {
        return leafSetEntryNodeCnSnParser;
    }

    @Override
    public ToNormalizedNodeParser<Node<?>, MapEntryNode, ListSchemaNode> getMapEntryNodeParser() {
        return mapEntryNodeCnSnParser;
    }

    @Override
    public ToNormalizedNodeParser<Node<?>, AnyXmlNode, AnyXmlSchemaNode> getAnyXmlNodeParser() {
        return anyXmlNodeCnSnParser;
    }
}
