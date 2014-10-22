package org.opendaylight.yangtools.yang.data.composite.node.schema.parser;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.composite.node.schema.TestUtils;
import org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.parser.CnSnToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Bug2180Test {
    private Module simpleContainerModule;
    private Module bug2111TestModule;

    @Before
    public void init() throws URISyntaxException {
        Set<Module> modules = TestUtils.loadModulesFrom("/cnsn-to-normalized-node/yang");
        simpleContainerModule = TestUtils.resolveModule("simple-container-yang", modules);
        bug2111TestModule = TestUtils.resolveModule("bug2180-test-module", modules);
    }

    @Test
    public void orderedListParseTest() throws DataValidationFailedException, URISyntaxException {
        ContainerSchemaNode topContainerSchemaNode = (ContainerSchemaNode) TestUtils.resolveDataSchemaNode("top", bug2111TestModule);
        ListSchemaNode aceListSchemaNode = (ListSchemaNode) topContainerSchemaNode.getDataChildByName("ordered-list");
        ToNormalizedNodeParser<Node<?>, MapNode, ListSchemaNode> mapNodeParser =
                CnSnToNormalizedNodeParserFactory.getInstance().getMapNodeParser();
        MapNode mapNode = mapNodeParser.parse(new ArrayList<Node<?>>(), aceListSchemaNode);
        assertTrue(mapNode instanceof OrderedMapNode);
    }

    @Test
    public void unorderedListParseTest() throws DataValidationFailedException, URISyntaxException {
        ContainerSchemaNode topContainerSchemaNode = (ContainerSchemaNode) TestUtils.resolveDataSchemaNode("cont", simpleContainerModule);
        ListSchemaNode aceListSchemaNode = (ListSchemaNode) topContainerSchemaNode.getDataChildByName("lst2");
        ToNormalizedNodeParser<Node<?>, MapNode, ListSchemaNode> mapNodeParser =
                CnSnToNormalizedNodeParserFactory.getInstance().getMapNodeParser();
        MapNode mapNode = mapNodeParser.parse(new ArrayList<Node<?>>(), aceListSchemaNode);
        assertFalse(mapNode instanceof OrderedMapNode);
    }

    @Test
    public void orderedLeafListParseTest() throws DataValidationFailedException, URISyntaxException {
        ContainerSchemaNode topContainerSchemaNode = (ContainerSchemaNode) TestUtils.resolveDataSchemaNode("top", bug2111TestModule);
        LeafListSchemaNode aceListSchemaNode = (LeafListSchemaNode) topContainerSchemaNode.getDataChildByName("ordered-leaf-list");
        ToNormalizedNodeParser<Node<?>, LeafSetNode<?>, LeafListSchemaNode> leafSetNodeParser =
                CnSnToNormalizedNodeParserFactory.getInstance().getLeafSetNodeParser();
        LeafSetNode<?> leafSetNode = leafSetNodeParser.parse(new ArrayList<Node<?>>(), aceListSchemaNode);
        assertTrue(leafSetNode instanceof OrderedLeafSetNode);
    }

    @Test
    public void unorderedLeafListParseTest() throws DataValidationFailedException, URISyntaxException {
        ContainerSchemaNode topContainerSchemaNode = (ContainerSchemaNode) TestUtils.resolveDataSchemaNode("cont", simpleContainerModule);
        LeafListSchemaNode aceListSchemaNode = (LeafListSchemaNode) topContainerSchemaNode.getDataChildByName("lflst1");
        ToNormalizedNodeParser<Node<?>, LeafSetNode<?>, LeafListSchemaNode> leafSetNodeParser =
                CnSnToNormalizedNodeParserFactory.getInstance().getLeafSetNodeParser();
        LeafSetNode<?> leafSetNode = leafSetNodeParser.parse(new ArrayList<Node<?>>(), aceListSchemaNode);
        assertFalse(leafSetNode instanceof OrderedLeafSetNode);
    }

}
