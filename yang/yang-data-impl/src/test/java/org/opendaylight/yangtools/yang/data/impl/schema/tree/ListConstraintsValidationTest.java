package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;

import com.google.common.base.Optional;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListConstraintsValidationTest {
    private static final Logger LOG = LoggerFactory.getLogger(ListConstraintsValidationTest.class);

    private static final String CONSTRAINTS_VALIDATION_TEST_YANG = "/list-constraints-validation-test-model.yang";
    private SchemaContext schemaContext;
    private RootModificationApplyOperation rootOper;

    private static final QName MASTER_CONTAINER_QNAME = QName.create("urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model", "2015-02-02",
            "master-container");
//    private static final QName RANGE_INT8_QNAME = QName.create(MASTER_CONTAINER_QNAME, "range-int8");
//    private static final QName REGEX_STRING_QNAME = QName.create(MASTER_CONTAINER_QNAME, "regex-string");
//    private static final QName LENGTH_STRING_QNAME = QName.create(MASTER_CONTAINER_QNAME, "length-string");
//    private static final QName LENGTH_BINARY_QNAME = QName.create(MASTER_CONTAINER_QNAME, "length-binary");
//    private static final QName TWO_TYPE_UNION_QNAME = QName.create(MASTER_CONTAINER_QNAME, "two-type-union");
//    private static final QName DERIVED_STRING_QNAME = QName.create(MASTER_CONTAINER_QNAME, "derived-string");
//    private static final QName ENUM_TEST_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "enum-test-leaf");
//    private static final QName RANGE_DECIMAL_QNAME = QName.create(MASTER_CONTAINER_QNAME, "range-decimal");
//    private static final QName RANGE_UINT64_QNAME = QName.create(MASTER_CONTAINER_QNAME, "range-uint64");
    private static final QName MIN_MAX_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-list");
    private static final QName KEY_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "key-leaf");


    private static final YangInstanceIdentifier MASTER_CONTAINER_PATH = YangInstanceIdentifier.of(MASTER_CONTAINER_QNAME);
//    private static final YangInstanceIdentifier RANGE_INT8_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
//            .node(RANGE_INT8_QNAME).build();
//    private static final YangInstanceIdentifier REGEX_STRING_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
//            .node(REGEX_STRING_QNAME).build();
//    private static final YangInstanceIdentifier LENGTH_STRING_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
//            .node(LENGTH_STRING_QNAME).build();
//    private static final YangInstanceIdentifier LENGTH_BINARY_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
//            .node(LENGTH_BINARY_QNAME).build();
//    private static final YangInstanceIdentifier TWO_TYPE_UNION_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
//            .node(TWO_TYPE_UNION_QNAME).build();
//    private static final YangInstanceIdentifier DERIVED_STRING_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
//            .node(DERIVED_STRING_QNAME).build();
//    private static final YangInstanceIdentifier ENUM_TEST_LEAF_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
//            .node(ENUM_TEST_LEAF_QNAME).build();
//    private static final YangInstanceIdentifier RANGE_DECIMAL_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
//            .node(RANGE_DECIMAL_QNAME).build();
//    private static final YangInstanceIdentifier RANGE_UINT64_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
//            .node(RANGE_UINT64_QNAME).build();
    private static final YangInstanceIdentifier MIN_MAX_LIST_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(MIN_MAX_LIST_QNAME).build();

    @Before
    public void prepare() {
        schemaContext = createTestContext();
        assertNotNull("Schema context must not be null.", schemaContext);
        rootOper = RootModificationApplyOperation.from(SchemaAwareApplyOperation.from(schemaContext));
    }

    public static final InputStream getDatastoreTestInputStream() {
        return getInputStream(CONSTRAINTS_VALIDATION_TEST_YANG);
    }

    private static InputStream getInputStream(final String resourceName) {
        return TestModel.class.getResourceAsStream(CONSTRAINTS_VALIDATION_TEST_YANG);
    }

    public static SchemaContext createTestContext() {
        YangParserImpl parser = new YangParserImpl();
        Set<Module> modules = parser.parseYangModelsFromStreams(Collections.singletonList(getDatastoreTestInputStream()));
        return parser.resolveSchemaContext(modules);
    }

    @Test
    public void MinMaxListTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        final MapEntryNode mmListMapEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, KEY_LEAF_QNAME, "devad");
        final MapEntryNode mmListMapEntryNode2 = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, KEY_LEAF_QNAME, "zidaň");
        final MapEntryNode mmListMapEntryNode3 = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, KEY_LEAF_QNAME, "motorko");
        final MapNode mmListMapNode = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME))
                .withChild(mmListMapEntryNode).build();

        final YangInstanceIdentifier devadPath = MIN_MAX_LIST_PATH.node(mmListMapEntryNode.getIdentifier());
        final YangInstanceIdentifier zidanPath = MIN_MAX_LIST_PATH.node(mmListMapEntryNode2.getIdentifier());
        final YangInstanceIdentifier motorkoPath = MIN_MAX_LIST_PATH.node(mmListMapEntryNode3.getIdentifier());

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(MIN_MAX_LIST_PATH, mmListMapNode);
        modificationTree.merge(zidanPath, mmListMapEntryNode2);
        modificationTree.write(motorkoPath, mmListMapEntryNode3);
        modificationTree.delete(motorkoPath);


        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        final InMemoryDataTreeSnapshot inMemoryDataTreeSnapshot = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode<?, ?>> normalizedNodeOptional = inMemoryDataTreeSnapshot.readNode(MIN_MAX_LIST_PATH);

        modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(motorkoPath, mmListMapEntryNode3);

        inMemoryDataTree.validate(modificationTree);
        prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.delete(motorkoPath);
        modificationTree.delete(devadPath);

        inMemoryDataTree.validate(modificationTree);
        prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }
}
