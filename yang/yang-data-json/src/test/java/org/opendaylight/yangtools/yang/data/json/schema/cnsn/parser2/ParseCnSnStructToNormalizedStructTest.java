package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.controller.sal.rest.impl.JsonToCompositeNodeProvider;
import org.opendaylight.controller.sal.restconf.impl.NodeWrapper;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.json.schema.JsonTestUtils;
import org.opendaylight.yangtools.yang.data.json.schema.json.JsonJsonUtils;
import org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser.ChoiceNodeCnSnParser;
import org.opendaylight.yangtools.yang.data.impl.schema.universal.parser.ContainerNodeDomParser;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.common.collect.Lists;

public class ParseCnSnStructure {

    private static SchemaContext schemaContext;
    private static InputStream jsonData;
    private static DataSchemaNode resolvedDataSchemaNode;

    @BeforeClass
    public static void loadData() {
        Set<Module> modules = JsonTestUtils.loadModulesFrom("/json-to-normalize-node/yang");
        Module resolvedModule = JsonTestUtils.resolveModule("simple-container-yang", modules);
        resolvedDataSchemaNode = JsonTestUtils.resolveDataSchemaNode("cont", resolvedModule);
        schemaContext = JsonTestUtils.loadSchemaContext(modules);
        jsonData = ParseCnSnStructure.class.getResourceAsStream("/json-to-normalize-node/json/simple-container.json");

    }

    @Test
    public void testCnSnToNormalizedNode() throws WebApplicationException, IOException, URISyntaxException {

        // ObjectMapper mapper = new ObjectMapper();
        // JsonNode rootNode = mapper.readTree(jsonData);

        // JsonJsonUtils.setSchemaContext(schemaContext);
        // List<JsonNode> root = new ArrayList<JsonNode>();
        // root.add(rootNode);
        // ContainerNode containerNode = new
        // ContainerNodeJsonParser().fromJsonDom(
        // root, (ContainerSchemaNode) resolvedDataSchemaNode,
        // JsonJsonUtils.defaultValueCodecProvider());

        // Iterator<Entry<String, JsonNode>> fields = rootNode.getFields();

        // CompositeNode compNode prepareStructure

        CompositeNode compNode = JsonToCompositeNodeProvider.INSTANCE.readFrom(null, null, null, null, null, jsonData);

        JsonTestUtils.addDummyNamespaceToAllNodes((NodeWrapper<?>) compNode, "simple:container:yang");

        List<Node<?>> lst = new ArrayList<Node<?>>();
        lst.add(compNode);
        new ContainerNodeCnSnParser().fromCnSn(lst, (ContainerSchemaNode) resolvedDataSchemaNode);

    }
}
