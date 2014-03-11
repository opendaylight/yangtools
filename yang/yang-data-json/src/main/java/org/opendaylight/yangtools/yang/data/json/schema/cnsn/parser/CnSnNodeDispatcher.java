package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class CnSnNodeDispatcher extends NodeParserDispatcher.BaseNodeParserDispatcher<Node<?>> {

    private final ContainerNodeCnSnParser containerNodeCnSnParser;
    private final LeafNodeCnSnParser leafNodeCnSnParser;
    private final LeafSetNodeCnSnParser leafSetNodeCnSnParser;
    private final MapNodeCnSnParser mapNodeCnSnParser;
    private final ChoiceNodeCnSnParser choiceNodeCnSnParser;
    private final AugmentationNodeCnSnParser augmentationNodeCnSnParser;

    private static CnSnNodeDispatcher instance = null;

    private CnSnNodeDispatcher() {
        leafNodeCnSnParser = new LeafNodeCnSnParser();
        containerNodeCnSnParser = new ContainerNodeCnSnParser();
        leafSetNodeCnSnParser = new LeafSetNodeCnSnParser();
        mapNodeCnSnParser = new MapNodeCnSnParser();
        choiceNodeCnSnParser = new ChoiceNodeCnSnParser();
        augmentationNodeCnSnParser = new AugmentationNodeCnSnParser();
    }

    @Override
    protected ToNormalizedNodeParser<Node<?>, ContainerNode, ContainerSchemaNode> getContainerNodeDomParser() {
        return containerNodeCnSnParser;
    }

    @Override
    protected ToNormalizedNodeParser<Node<?>, LeafNode<?>, LeafSchemaNode> getLeafNodeDomParser() {
        return leafNodeCnSnParser;
    }

    @Override
    protected ToNormalizedNodeParser<Node<?>, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeDomParser() {
        return leafSetNodeCnSnParser;
    }

    @Override
    protected ToNormalizedNodeParser<Node<?>, MapNode, ListSchemaNode> getMapNodeDomParser() {
        return mapNodeCnSnParser;
    }

    @Override
    protected ToNormalizedNodeParser<Node<?>, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode, ChoiceNode> getChoiceNodeDomParser() {
        return choiceNodeCnSnParser;
    }

    @Override
    protected ToNormalizedNodeParser<Node<?>, AugmentationNode, AugmentationSchema> getAugmentationNodeDomParser() {
        return augmentationNodeCnSnParser;
    }

    public static NodeParserDispatcher<Node<?>> getInstance() {
        if (instance == null) {
            instance = new CnSnNodeDispatcher();
        }
        return instance;
    }
}
