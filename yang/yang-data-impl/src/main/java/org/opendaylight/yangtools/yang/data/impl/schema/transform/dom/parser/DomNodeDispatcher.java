package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.w3c.dom.Element;

public class DomNodeDispatcher extends NodeParserDispatcher.BaseNodeParserDispatcher<Element> {

        private final ContainerNodeDomParser containerNodeDomParser;
        private final LeafNodeDomParser leafNodeDomParser;
        private final LeafSetNodeDomParser leafSetNodeDomParser;
        private final MapNodeDomParser mapNodeDomParser;
        private final ChoiceNodeDomParser choiceNodeDomParser;
        private final AugmentationNodeDomParser augmentationNodeDomParser;

        private static DomNodeDispatcher instance = null;

        private DomNodeDispatcher(XmlCodecProvider codecProvider) {
            leafNodeDomParser = new LeafNodeDomParser(codecProvider);
            containerNodeDomParser = new ContainerNodeDomParser(codecProvider);
            leafSetNodeDomParser = new LeafSetNodeDomParser(codecProvider);
            mapNodeDomParser = new MapNodeDomParser(codecProvider);
            choiceNodeDomParser = new ChoiceNodeDomParser(codecProvider);
            augmentationNodeDomParser = new AugmentationNodeDomParser(codecProvider);
        }

        @Override
        protected ToNormalizedNodeParser<Element, ContainerNode, ContainerSchemaNode> getContainerNodeDomParser() {
            return containerNodeDomParser;
        }

        @Override
        protected ToNormalizedNodeParser<Element, LeafNode<?>, LeafSchemaNode> getLeafNodeDomParser() {
            return leafNodeDomParser;
        }

        @Override
        protected ToNormalizedNodeParser<Element, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeDomParser() {
            return leafSetNodeDomParser;
        }

        @Override
        protected ToNormalizedNodeParser<Element, MapNode, ListSchemaNode> getMapNodeDomParser() {
            return mapNodeDomParser;
        }

        @Override
        protected ToNormalizedNodeParser<Element, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode, ChoiceNode> getChoiceNodeDomParser() {
            return choiceNodeDomParser;
        }

        @Override
        protected ToNormalizedNodeParser<Element, AugmentationNode, AugmentationSchema> getAugmentationNodeDomParser() {
            return augmentationNodeDomParser;
        }

        public static NodeParserDispatcher<Element> getInstance(XmlCodecProvider codecProvider) {
            if (instance == null) {
                instance = new DomNodeDispatcher(codecProvider);
            }
            return instance;
        }
}
