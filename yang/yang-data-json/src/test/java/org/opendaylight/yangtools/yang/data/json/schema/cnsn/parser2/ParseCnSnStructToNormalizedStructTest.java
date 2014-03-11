package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser2;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.NodeFactory.createImmutableCompositeNode;
import static org.opendaylight.yangtools.yang.data.impl.NodeFactory.createImmutableSimpleNode;
import static org.opendaylight.yangtools.yang.data.impl.NodeFactory.createMutableCompositeNode;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.json.schema.TestUtils;
import org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser.ContainerNodeCnSnParser;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

public class ParseCnSnStructToNormalizedStructTest {

    private static DataSchemaNode resolvedDataSchemaNode;
    private static final String NAMESPACE = "simple:container:yang";
    private static Date revision;

    static {
        try {
            revision = new SimpleDateFormat("yyyy-MM-dd").parse("2013-11-12");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void loadData() {
        Set<Module> modules = TestUtils.loadModulesFrom("/cnsn-to-normalized-node/yang");
        Module resolvedModule = TestUtils.resolveModule("simple-container-yang", modules);
        resolvedDataSchemaNode = TestUtils.resolveDataSchemaNode("cont", resolvedModule);
    }

    @Test
    public void testCnSnToNormalizedNode() {

        CompositeNode compNode = prepareCompositeNodeData();

        List<Node<?>> lst = new ArrayList<Node<?>>();
        lst.add(compNode);
        ContainerNode parsed = new ContainerNodeCnSnParser().parse(lst, (ContainerSchemaNode) resolvedDataSchemaNode);

        ContainerNode prepareExpectedStruct = prepareExpectedStruct();
        // System.out.println(prepareExpectedStruct);
        // System.out.println(parsed);
        assertEquals(prepareExpectedStruct, parsed);
    }

    /**
     * Prepare composite node structure according to
     * /cnsn-to-normalized-node/simple-conainer.json
     */
    private CompositeNode prepareCompositeNodeData() {
        MutableCompositeNode cont = createMutableCompositeNode(new QName(URI.create(NAMESPACE), revision, "cont"),
                null, null, null, null);

        // cont1
        List<Node<?>> contChilds = new ArrayList<>();
        contChilds.add(createImmutableCompositeNode(new QName(URI.create(NAMESPACE), revision, "cont1"), cont,
                Collections.<Node<?>> emptyList()));

        // cont2
        MutableCompositeNode cont2 = createMutableCompositeNode(new QName(URI.create(NAMESPACE), revision, "cont2"),
                cont, null, null, null);
        List<Node<?>> cont2Childs = new ArrayList<>();
        cont2Childs.add(createImmutableSimpleNode(new QName(URI.create(NAMESPACE), revision, "lf21"), cont2,
                "value in cont2/lf21"));
        cont2.setValue(cont2Childs);
        contChilds.add(cont2);

        // lst1
        contChilds.add(createImmutableCompositeNode(new QName(URI.create(NAMESPACE), revision, "lst1"), cont,
                Collections.<Node<?>> emptyList(), null));

        // lst2
        MutableCompositeNode lst2_1 = createMutableCompositeNode(new QName(URI.create(NAMESPACE), revision, "lst2"),
                cont, null, null, null);
        List<Node<?>> lst2_1Childs = new ArrayList<>();
        lst2_1Childs.add(createImmutableSimpleNode(new QName(URI.create(NAMESPACE), revision, "lf21"), lst2_1,
                "some value21"));
        lst2_1.setValue(lst2_1Childs);
        contChilds.add(lst2_1);

        MutableCompositeNode lst2_2 = createMutableCompositeNode(new QName(URI.create(NAMESPACE), revision, "lst2"),
                cont, null, null, null);
        List<Node<?>> lst2_2Childs = new ArrayList<>();
        lst2_2Childs.add(createImmutableSimpleNode(new QName(URI.create(NAMESPACE), revision, "lf22"), lst2_2,
                "some value22"));
        lst2_2.setValue(lst2_2Childs);
        contChilds.add(lst2_2);

        // lflst1
        contChilds
                .add(createImmutableSimpleNode(new QName(URI.create(NAMESPACE), revision, "lflst1"), cont, "lflst1_1"));
        contChilds
                .add(createImmutableSimpleNode(new QName(URI.create(NAMESPACE), revision, "lflst1"), cont, "lflst1_2"));

        // lf1
        contChilds.add(createImmutableSimpleNode(new QName(URI.create(NAMESPACE), revision, "lf1"), cont, "lf1"));

        cont.setValue(contChilds);
        return cont;
    }

    private ContainerNode prepareExpectedStruct() {
        DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder = Builders.containerBuilder();
        containerBuilder.withNodeIdentifier(getNodeIdentifier("cont"));
        containerBuilder.withChild(Builders.containerBuilder().withNodeIdentifier(getNodeIdentifier("cont1")).build());
        containerBuilder.withChild(Builders
                .containerBuilder()
                .withNodeIdentifier(getNodeIdentifier("cont2"))
                .withChild(
                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("lf21"))
                                .withValue("value in cont2/lf21").build()).build());
        CollectionNodeBuilder<MapEntryNode, MapNode> lst1 = Builders.mapBuilder().withNodeIdentifier(
                getNodeIdentifier("lst1"));
        lst1.withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(getNodeIdentifierPredicate("lst1", new HashMap<String, Object>()))
                .withValue(Collections.<DataContainerChild<? extends PathArgument, ?>> emptyList()).build());
        containerBuilder.withChild(lst1.build());

        CollectionNodeBuilder<MapEntryNode, MapNode> lst2 = Builders.mapBuilder().withNodeIdentifier(
                getNodeIdentifier("lst2"));

        Map<String, Object> lst2_1 = new HashMap<>();
        lst2_1.put("lf21", "some value21");

        List<DataContainerChild<? extends PathArgument, ?>> lst2_1_values = new ArrayList<>();
        lst2_1_values.add(Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("lf21"))
                .withValue("some value21").build());
        lst2.withChild(Builders.mapEntryBuilder().withNodeIdentifier(getNodeIdentifierPredicate("lst2", lst2_1))
                .withValue(lst2_1_values).build());

        Map<String, Object> lst2_2 = new HashMap<>();
        lst2_2.put("lf22", "some value22");
        List<DataContainerChild<? extends PathArgument, ?>> lst2_2_values = new ArrayList<>();
        lst2_2_values.add(Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("lf22"))
                .withValue("some value22").build());
        lst2.withChild(Builders.mapEntryBuilder().withNodeIdentifier(getNodeIdentifierPredicate("lst2", lst2_2))
                .withValue(lst2_2_values).build());
        containerBuilder.withChild(lst2.build());

        ListNodeBuilder<Object, LeafSetEntryNode<Object>> lflst1 = Builders.leafSetBuilder().withNodeIdentifier(
                getNodeIdentifier("lflst1"));
        lflst1.withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(getNodeIdentifier("lflst1", "lflst1_1"))
                .withValue("lflst1_1").build());
        lflst1.withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(getNodeIdentifier("lflst1", "lflst1_2"))
                .withValue("lflst1_2").build());
        containerBuilder.withChild(lflst1.build());

        containerBuilder.withChild(Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("lf1")).withValue("lf1")
                .build());

        ContainerNode build = containerBuilder.build();
        return build;
    }

    private static InstanceIdentifier.NodeIdentifier getNodeIdentifier(String localName) {
        return new InstanceIdentifier.NodeIdentifier(new QName(URI.create(NAMESPACE), revision, localName));
    }

    private static InstanceIdentifier.NodeWithValue getNodeIdentifier(String localName, Object value) {
        return new InstanceIdentifier.NodeWithValue(new QName(URI.create(NAMESPACE), revision, localName), value);
    }

    private static InstanceIdentifier.NodeIdentifierWithPredicates getNodeIdentifierPredicate(String localName,
            Map<String, Object> keys) {
        Map<QName, Object> predicate = new HashMap<>();
        for (String key : keys.keySet()) {
            predicate.put(new QName(URI.create(NAMESPACE), revision, key), keys.get(key));
        }

        return new InstanceIdentifier.NodeIdentifierWithPredicates(

        new QName(URI.create(NAMESPACE), revision, localName), predicate);
    }

}
