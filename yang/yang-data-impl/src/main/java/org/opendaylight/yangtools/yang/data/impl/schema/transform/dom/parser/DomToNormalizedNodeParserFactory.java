/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.LeafStrategy;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.ParsingStrategy;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Element;

public final class DomToNormalizedNodeParserFactory implements ToNormalizedNodeParserFactory<Element> {
    static final ParsingStrategy DEFAULT_STRATEGY = new DefaultParsingStrategy();

    private final AugmentationNodeDomParser augmentationNodeParser;
    private final ChoiceNodeDomParser choiceNodeParser;
    private final ContainerNodeDomParser containerNodeParser;
    private final LeafNodeDomParser leafNodeParser;
    private final LeafSetEntryNodeDomParser leafSetEntryNodeParser;
    private final LeafSetNodeDomParser leafSetNodeParser;
    private final MapNodeDomParser mapNodeParser;
    private final MapEntryNodeDomParser mapEntryNodeParser;
    private final UnkeyedListEntryNodeDomParser unkeyedListEntryNodeParser;
    private final UnkeyedListNodeDomParser unkeyedListNodeParser;
    private final OrderedListNodeDomParser orderedListNodeParser;
    private final AnyXmlDomParser anyXmlNodeParser;

    private DomToNormalizedNodeParserFactory(final XmlCodecProvider codecProvider, final SchemaContext schema) {
        leafNodeParser = new LeafNodeDomParser(codecProvider, schema);
        leafSetEntryNodeParser = new LeafSetEntryNodeDomParser(codecProvider, schema);
        leafSetNodeParser = new LeafSetNodeDomParser(leafSetEntryNodeParser);
        anyXmlNodeParser = new AnyXmlDomParser();

        final NodeParserDispatcher<Element> dispatcher = new NodeParserDispatcher.BaseNodeParserDispatcher<Element>(this) {

        };

        containerNodeParser = new ContainerNodeDomParser(dispatcher);
        mapEntryNodeParser = new MapEntryNodeDomParser(dispatcher);
        mapNodeParser = new MapNodeDomParser(mapEntryNodeParser, DEFAULT_STRATEGY);
        orderedListNodeParser = new OrderedListNodeDomParser(mapEntryNodeParser, DEFAULT_STRATEGY);
        unkeyedListEntryNodeParser = new UnkeyedListEntryNodeDomParser(dispatcher);
        unkeyedListNodeParser = new UnkeyedListNodeDomParser(unkeyedListEntryNodeParser, DEFAULT_STRATEGY);
        choiceNodeParser = new ChoiceNodeDomParser(dispatcher);
        augmentationNodeParser = new AugmentationNodeDomParser(dispatcher);
    }

    private DomToNormalizedNodeParserFactory(final XmlCodecProvider codecProvider, final SchemaContext schema,
                                             final ParsingStrategy parsingStrategy, final LeafStrategy leafStrategy) {
        leafNodeParser = new LeafNodeDomParser(codecProvider, schema, leafStrategy);
        leafSetEntryNodeParser = new LeafSetEntryNodeDomParser(codecProvider, schema, leafStrategy);
        leafSetNodeParser = new LeafSetNodeDomParser(leafSetEntryNodeParser);
        anyXmlNodeParser = new AnyXmlDomParser();

        final NodeParserDispatcher<Element> dispatcher = new NodeParserDispatcher.BaseNodeParserDispatcher<Element>(this) {

        };

        containerNodeParser = new ContainerNodeDomParser(dispatcher, parsingStrategy);
        mapEntryNodeParser = new MapEntryNodeDomParser(dispatcher, parsingStrategy);
        mapNodeParser = new MapNodeDomParser(mapEntryNodeParser, parsingStrategy);
        orderedListNodeParser = new OrderedListNodeDomParser(mapEntryNodeParser, parsingStrategy);
        unkeyedListEntryNodeParser = new UnkeyedListEntryNodeDomParser(dispatcher);
        unkeyedListNodeParser = new UnkeyedListNodeDomParser(unkeyedListEntryNodeParser, parsingStrategy);
        choiceNodeParser = new ChoiceNodeDomParser(dispatcher);
        augmentationNodeParser = new AugmentationNodeDomParser(dispatcher);
    }

    @Deprecated
    private DomToNormalizedNodeParserFactory(final XmlCodecProvider codecProvider) {
        leafNodeParser = new LeafNodeDomParser(codecProvider);
        leafSetEntryNodeParser = new LeafSetEntryNodeDomParser(codecProvider);
        leafSetNodeParser = new LeafSetNodeDomParser(leafSetEntryNodeParser);
        anyXmlNodeParser = new AnyXmlDomParser();

        final NodeParserDispatcher<Element> dispatcher = new NodeParserDispatcher.BaseNodeParserDispatcher<Element>(this) {

        };

        containerNodeParser = new ContainerNodeDomParser(dispatcher);
        mapEntryNodeParser = new MapEntryNodeDomParser(dispatcher);
        mapNodeParser = new MapNodeDomParser(mapEntryNodeParser, DEFAULT_STRATEGY);
        orderedListNodeParser = new OrderedListNodeDomParser(mapEntryNodeParser, DEFAULT_STRATEGY);
        unkeyedListEntryNodeParser = new UnkeyedListEntryNodeDomParser(dispatcher);
        unkeyedListNodeParser = new UnkeyedListNodeDomParser(unkeyedListEntryNodeParser, DEFAULT_STRATEGY);
        choiceNodeParser = new ChoiceNodeDomParser(dispatcher);
        augmentationNodeParser = new AugmentationNodeDomParser(dispatcher);
    }

    public static DomToNormalizedNodeParserFactory getInstance(final XmlCodecProvider codecProvider, final SchemaContext schema) {
        return new DomToNormalizedNodeParserFactory(codecProvider, schema);
    }

    public static DomToNormalizedNodeParserFactory getInstance(final XmlCodecProvider codecProvider, final SchemaContext schema,
                                                               final ParsingStrategy parsingStrategy, final LeafStrategy leafStrategy) {
        return new DomToNormalizedNodeParserFactory(codecProvider, schema, parsingStrategy, leafStrategy);
    }

    @Deprecated
    public static DomToNormalizedNodeParserFactory getInstance(final XmlCodecProvider codecProvider) {
        return new DomToNormalizedNodeParserFactory(codecProvider);
    }

    @Override
    public ToNormalizedNodeParser<Element, AugmentationNode, AugmentationSchema> getAugmentationNodeParser() {
        return augmentationNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, ChoiceNode, ChoiceSchemaNode> getChoiceNodeParser() {
        return choiceNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, ContainerNode, ContainerSchemaNode> getContainerNodeParser() {
        return containerNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, LeafNode<?>, LeafSchemaNode> getLeafNodeParser() {
        return leafNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, LeafSetEntryNode<?>, LeafListSchemaNode> getLeafSetEntryNodeParser() {
        return leafSetEntryNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeParser() {
        return leafSetNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, MapEntryNode, ListSchemaNode> getMapEntryNodeParser() {
        return mapEntryNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, MapNode, ListSchemaNode> getMapNodeParser() {
        return mapNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, UnkeyedListNode, ListSchemaNode> getUnkeyedListNodeParser() {
        return unkeyedListNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, UnkeyedListEntryNode, ListSchemaNode> getUnkeyedListEntryNodeParser() {
        return unkeyedListEntryNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, OrderedMapNode, ListSchemaNode> getOrderedListNodeParser() {
        return orderedListNodeParser;
    }

    @Override
    public ToNormalizedNodeParser<Element, AnyXmlNode, AnyXmlSchemaNode> getAnyXmlNodeParser() {
        return anyXmlNodeParser;
    }

    private static class DefaultParsingStrategy<N extends DataContainerNode<?>> implements ParsingStrategy<N> {

        @Override
        public void prepareAttribues(Map<QName, String> attribues, DataContainerNodeBuilder<?, N> containerBuilder) {}

        @Override
        public N applyStrategy(DataContainerNodeBuilder<?, N> containerBuilder) {
            return containerBuilder.build();
        }

        @Override
        public void addListIdentifier(QName listIdent) {}

        @Override
        public void popListIdentifier() {}

    }
}
