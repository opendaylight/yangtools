package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.json.schema.TestUtils;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

public class ParseCnSnStructToNormalizedStructTest {

    private static DataSchemaNode resolvedDataSchemaNode;



    @BeforeClass
    public static void loadData() {
        Set<Module> modules = TestUtils.loadModulesFrom("/cnsn-to-normalized-node/yang");
        Module resolvedModule = TestUtils.resolveModule("simple-container-yang", modules);
        resolvedDataSchemaNode = TestUtils.resolveDataSchemaNode("cont", resolvedModule);
    }

    @Test
    public void testCnSnToNormalizedNode() throws URISyntaxException {

        CompositeNode compNode = TestUtils.prepareCompositeNodeStruct();

        List<Node<?>> lst = new ArrayList<Node<?>>();
        lst.add(compNode);
        ContainerNode parsed = new ContainerNodeCnSnParser().parse(lst, (ContainerSchemaNode) resolvedDataSchemaNode);

        ContainerNode prepareExpectedStruct = TestUtils.prepareNormalizedNodeStruct();
        assertEquals(prepareExpectedStruct, parsed);
    }


}
