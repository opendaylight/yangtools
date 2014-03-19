package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser2;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.json.schema.JsonTestUtils;
import org.opendaylight.yangtools.yang.data.json.schema.json.serializer.ContainerNodeNormSerializer;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class SerlializeNormalizedStructToCnSnTest {

	private static final QName ROOT_QNAME = QName.create(
			"urn:opendaylight:controller:yang:data:test", "2014-03-18", "root");
	private static final QName LIST_A_QNAME = QName
			.create(ROOT_QNAME, "list_a");
	private static final QName LIST_B_QNAME = QName
			.create(ROOT_QNAME, "list_b");
	private static final QName LEAF_A_QNAME = QName
			.create(ROOT_QNAME, "leaf_a");
	private static final QName LEAF_B_QNAME = QName
			.create(ROOT_QNAME, "leaf_b");
	private static final QName CHOICE_A_QNAME = QName.create(ROOT_QNAME,
			"choice_a");
	private static final String FOO = "foo";
	private static final String BAR = "bar";
	private static final String ONE = "one";
	private static final String TWO = "two";

	private static final InstanceIdentifier LIST_A_FOO_PATH = InstanceIdentifier
			.builder().node(ROOT_QNAME).node(LIST_A_QNAME)
			.nodeWithKey(LIST_A_QNAME, LEAF_A_QNAME, FOO).build();
	private static final InstanceIdentifier LIST_B_TWO_PATH = InstanceIdentifier
			.builder().node(LIST_A_QNAME)
			.nodeWithKey(LIST_A_QNAME, LEAF_A_QNAME, BAR).node(LEAF_B_QNAME)
			.nodeWithKey(LIST_B_QNAME, LEAF_B_QNAME, TWO).build();

	private static DataSchemaNode resolvedDataSchemaNode;
	private static final String NAMESPACE = "container:with:list";
	private static Date revision;

	@BeforeClass
	public void setUp() throws Exception {
		Set<Module> modules = JsonTestUtils
				.loadModulesFrom("/normalized-node-to-cnsn");
		Module resolveModule = JsonTestUtils.resolveModule(
				"container-with-list", modules);
		resolvedDataSchemaNode = JsonTestUtils.resolveDataSchemaNode("root",
				resolveModule);
	}

	@Test
	public void testNormalizeNodeToCnSn() {

		ContainerNodeNormSerializer contSer = new ContainerNodeNormSerializer();
		ContainerNode containerNode = createContainerNode();
		
		List<Node<?>> nodes = contSer.serialize((ContainerSchemaNode)resolvedDataSchemaNode, containerNode);
		CompositeNode compNode = (CompositeNode)nodes.get(0);
		
		
	}

	public ContainerNode createContainerNode() {
		// container
		DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> builder = Builders
				.containerBuilder().withNodeIdentifier(
						new NodeIdentifier(ROOT_QNAME));

		// leaf list
		MapEntryNode leaf_a = Builders
				.mapEntryBuilder()
				.withNodeIdentifier(
						new NodeIdentifierWithPredicates(LIST_A_QNAME,
								LEAF_A_QNAME, FOO)).build();

		// list
		CollectionNodeBuilder<MapEntryNode, MapNode> list_a = Builders
				.mapBuilder().withChild(leaf_a)
				.withNodeIdentifier(new NodeIdentifier(LIST_A_QNAME));

		ContainerNode contNode = builder.withChild(list_a.build()).build();
		
		return contNode;
	}

}
