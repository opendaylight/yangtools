package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueConstraintsValidationTest {
    private static final Logger LOG = LoggerFactory.getLogger(ValueConstraintsValidationTest.class);

    private static final String CONSTRAINTS_VALIDATION_TEST_YANG = "/value-constraints-validation-test-model.yang";
    private SchemaContext schemaContext;
    private RootModificationApplyOperation rootOper;

    private static final QName MASTER_CONTAINER_QNAME = QName.create("urn:opendaylight:params:xml:ns:yang:val-constraints-validation-test-model", "2015-02-02",
            "master-container");
    private static final QName RANGE_INT8_QNAME = QName.create(MASTER_CONTAINER_QNAME, "range-int8");
    private static final QName REGEX_STRING_QNAME = QName.create(MASTER_CONTAINER_QNAME, "regex-string");
    private static final QName LENGTH_STRING_QNAME = QName.create(MASTER_CONTAINER_QNAME, "length-string");
    private static final QName LENGTH_BINARY_QNAME = QName.create(MASTER_CONTAINER_QNAME, "length-binary");
    private static final QName TWO_TYPE_UNION_QNAME = QName.create(MASTER_CONTAINER_QNAME, "two-type-union");
    private static final QName DERIVED_STRING_QNAME = QName.create(MASTER_CONTAINER_QNAME, "derived-string");
    private static final QName ENUM_TEST_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "enum-test-leaf");
    private static final QName RANGE_DECIMAL_QNAME = QName.create(MASTER_CONTAINER_QNAME, "range-decimal");
    private static final QName RANGE_UINT64_QNAME = QName.create(MASTER_CONTAINER_QNAME, "range-uint64");


    private static final YangInstanceIdentifier MASTER_CONTAINER_PATH = YangInstanceIdentifier.of(MASTER_CONTAINER_QNAME);
    private static final YangInstanceIdentifier RANGE_INT8_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(RANGE_INT8_QNAME).build();
    private static final YangInstanceIdentifier REGEX_STRING_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(REGEX_STRING_QNAME).build();
    private static final YangInstanceIdentifier LENGTH_STRING_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(LENGTH_STRING_QNAME).build();
    private static final YangInstanceIdentifier LENGTH_BINARY_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(LENGTH_BINARY_QNAME).build();
    private static final YangInstanceIdentifier TWO_TYPE_UNION_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(TWO_TYPE_UNION_QNAME).build();
    private static final YangInstanceIdentifier DERIVED_STRING_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(DERIVED_STRING_QNAME).build();
    private static final YangInstanceIdentifier ENUM_TEST_LEAF_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(ENUM_TEST_LEAF_QNAME).build();
    private static final YangInstanceIdentifier RANGE_DECIMAL_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(RANGE_DECIMAL_QNAME).build();
    private static final YangInstanceIdentifier RANGE_UINT64_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(RANGE_UINT64_QNAME).build();

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
    public void RangedInt8TestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(RANGE_INT8_PATH, ImmutableNodes.leafNode(RANGE_INT8_QNAME, 5));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }

    @Test
    public void RangedInt8TestFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        try {
            modificationTree.write(RANGE_INT8_PATH, ImmutableNodes.leafNode(RANGE_INT8_QNAME, 6));
            Assert.fail("Exception should have been thrown!");
        } catch (Exception e) {
            LOG.debug("Exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }

    @Test
    public void RegexStringTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(REGEX_STRING_PATH, ImmutableNodes.leafNode(REGEX_STRING_QNAME, "00ffAA"));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }

    @Test
    public void RegexStringTestFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        try {
            modificationTree.write(REGEX_STRING_PATH, ImmutableNodes.leafNode(REGEX_STRING_QNAME, "007xXx"));
            Assert.fail("Exception should have been thrown!");
        } catch (Exception e) {
            LOG.debug("Exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }

    @Test
    public void LengthStringTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(LENGTH_STRING_PATH, ImmutableNodes.leafNode(LENGTH_STRING_QNAME,
                "OK string"));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }

    @Test
    public void LengthStringTestFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        try {
            modificationTree.write(LENGTH_STRING_PATH, ImmutableNodes.leafNode(LENGTH_STRING_QNAME,
                    "This string is too long for sure"));
            Assert.fail("Exception should have been thrown!");
        } catch (Exception e) {
            LOG.debug("Exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }

    @Test
    public void LengthBinaryTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        byte[] bytes = {(byte) 255, (byte) 255, (byte) 255};
        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(LENGTH_BINARY_PATH, ImmutableNodes.leafNode(LENGTH_BINARY_QNAME,
                bytes));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }

    @Test
    public void LengthBinaryTestFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        byte[] bytes = {(byte) 255};
        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        try {
            modificationTree.write(LENGTH_BINARY_PATH, ImmutableNodes.leafNode(LENGTH_BINARY_QNAME,
                    bytes));
            Assert.fail("Exception should have been thrown!");
        } catch (Exception e) {
            LOG.debug("Exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }

    @Test
    public void TwoTypeUnionTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(TWO_TYPE_UNION_PATH, ImmutableNodes.leafNode(TWO_TYPE_UNION_QNAME,
                "1234"));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }

    @Test
    public void TwoTypeUnionTestFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        try {
            modificationTree.write(TWO_TYPE_UNION_PATH, ImmutableNodes.leafNode(TWO_TYPE_UNION_QNAME,
                    "abcdefgh"));
            Assert.fail("Exception should have been thrown!");
        } catch (Exception e) {
            LOG.debug("Exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }

    @Test
    public void DerivedStringTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(DERIVED_STRING_PATH, ImmutableNodes.leafNode(DERIVED_STRING_QNAME,
                "1234"));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }

    @Test
    public void DerivedStringTestFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        try {
            modificationTree.write(DERIVED_STRING_PATH, ImmutableNodes.leafNode(DERIVED_STRING_QNAME,
                    "123456"));
            Assert.fail("Exception should have been thrown!");
        } catch (Exception e) {
            LOG.debug("Exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }

    @Test
    public void EnumTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(ENUM_TEST_LEAF_PATH, ImmutableNodes.leafNode(ENUM_TEST_LEAF_QNAME,
                "one"));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }

    @Test
    public void EnumTestFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        try {
            modificationTree.write(ENUM_TEST_LEAF_PATH, ImmutableNodes.leafNode(ENUM_TEST_LEAF_QNAME,
                    "four"));
            Assert.fail("Exception should have been thrown!");
        } catch (Exception e) {
            LOG.debug("Exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }

    @Test
    public void RangeDecimalTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(RANGE_DECIMAL_PATH, ImmutableNodes.leafNode(RANGE_DECIMAL_QNAME,
                new BigDecimal(7.4f)));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }

    @Test
    public void RangeDecimalTestFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        try {
            modificationTree.write(RANGE_DECIMAL_PATH, ImmutableNodes.leafNode(RANGE_DECIMAL_QNAME,
                    BigDecimal.valueOf(789.125)));
            Assert.fail("Exception should have been thrown!");
        } catch (Exception e) {
            LOG.debug("Exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }

    @Test
    public void RangeUint64TestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(RANGE_UINT64_PATH, ImmutableNodes.leafNode(RANGE_UINT64_QNAME,
                new BigInteger("18446744073709551615")));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);
    }

    @Test
    public void RangeUint64TestFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        try {
            modificationTree.write(RANGE_UINT64_PATH, ImmutableNodes.leafNode(RANGE_UINT64_QNAME,
                    -1));
            Assert.fail("Exception should have been thrown!");
        } catch (Exception e) {
            LOG.debug("Exception was thrown as expected: '{}' - '{}'", e.getClass(), e.getMessage());
        }
    }
}
