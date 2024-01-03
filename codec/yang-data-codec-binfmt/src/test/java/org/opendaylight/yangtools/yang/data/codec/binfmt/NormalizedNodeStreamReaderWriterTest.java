/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.xml.sax.InputSource;

class NormalizedNodeStreamReaderWriterTest {
    @ParameterizedTest
    @MethodSource
    void testNormalizedNodeStreaming(final NormalizedNodeStreamVersion version, final int size) throws Exception {
        final var bos = new ByteArrayOutputStream();
        final var nnout = version.newDataOutput(ByteStreams.newDataOutput(bos));

        final var testContainer = createTestContainer();
        nnout.writeNormalizedNode(testContainer);

        final var toaster = QName.create("http://netconfcentral.org/ns/toaster","2009-11-20","toaster");
        final var darknessFactor = QName.create("http://netconfcentral.org/ns/toaster","2009-11-20","darknessFactor");
        final var description = QName.create("http://netconfcentral.org/ns/toaster","2009-11-20","description");
        final var toasterNode = ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(toaster))
                .withChild(ImmutableNodes.leafNode(darknessFactor, "1000"))
                .withChild(ImmutableNodes.leafNode(description, largeString(20))).build();

        final var toasterContainer = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME)).withChild(toasterNode).build();
        nnout.writeNormalizedNode(toasterContainer);

        final byte[] bytes = bos.toByteArray();
        assertEquals(size, bytes.length);

        final var nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));

        assertEquals(testContainer, nnin.readNormalizedNode());
        assertEquals(toasterContainer, nnin.readNormalizedNode());
    }

    static List<Arguments> testNormalizedNodeStreaming() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 1_049_587));
    }

    private static ContainerNode createTestContainer() {
        final byte[] bytes1 = {1, 2, 3};
        final byte[] bytes2 = {};

        return TestModel.createBaseTestContainerBuilder()
            .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.BINARY_LEAF_LIST_QNAME))
                .withChild(ImmutableNodes.leafSetEntry(TestModel.BINARY_LEAF_LIST_QNAME, bytes1))
                .withChild(ImmutableNodes.leafSetEntry(TestModel.BINARY_LEAF_LIST_QNAME, bytes2))
                .build())
            .withChild(ImmutableNodes.leafNode(TestModel.SOME_BINARY_DATA_QNAME, new byte[]{1, 2, 3, 4}))
            .withChild(ImmutableNodes.leafNode(TestModel.EMPTY_QNAME, Empty.value()))
            .withChild(ImmutableNodes.newUserMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.ORDERED_LIST_QNAME))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(
                        NodeIdentifierWithPredicates.of(TestModel.ORDERED_LIST_ENTRY_QNAME, TestModel.ID_QNAME, 11))
                    .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, 11))
                    .build())
                .build())
            .build();
    }

    @ParameterizedTest
    @MethodSource
    void testYangInstanceIdentifierStreaming(final NormalizedNodeStreamVersion version, final int size)
            throws Exception {
        final var path = YangInstanceIdentifier.builder(TestModel.TEST_PATH).node(TestModel.OUTER_LIST_QNAME)
            .nodeWithKey(TestModel.INNER_LIST_QNAME, TestModel.ID_QNAME, 10).build();

        final var bos = new ByteArrayOutputStream();
        final var nnout = version.newDataOutput(ByteStreams.newDataOutput(bos));

        nnout.writeYangInstanceIdentifier(path);

        final byte[] bytes = bos.toByteArray();
        assertEquals(size, bytes.length);

        final var nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(path, nnin.readYangInstanceIdentifier());
    }

    static List<Arguments> testYangInstanceIdentifierStreaming() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 139));
    }

    @ParameterizedTest
    @MethodSource
    void testNormalizedNodeAndYangInstanceIdentifierStreaming(final NormalizedNodeStreamVersion version, final int size)
            throws Exception {
        final var testContainer = TestModel.createBaseTestContainerBuilder().build();
        final var path = YangInstanceIdentifier.builder(TestModel.TEST_PATH)
            .node(TestModel.OUTER_LIST_QNAME)
            .nodeWithKey(TestModel.INNER_LIST_QNAME, TestModel.ID_QNAME, 10)
            .build();

        final var bos = new ByteArrayOutputStream();
        try (var writer = version.newDataOutput(ByteStreams.newDataOutput(bos))) {
            writer.writeNormalizedNode(testContainer);
            writer.writeYangInstanceIdentifier(path);
        }

        final byte[] bytes = bos.toByteArray();
        assertEquals(size, bytes.length);

        final var reader = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(testContainer, reader.readNormalizedNode());
        assertEquals(path, reader.readYangInstanceIdentifier());
    }

    static List<Arguments> testNormalizedNodeAndYangInstanceIdentifierStreaming() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 794));
    }

    @Test
    void testInvalidNormalizedNodeStream() throws Exception {
        final var ex = assertThrows(InvalidNormalizedNodeStreamException.class,
            () -> NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(new byte[] { 1, 2, 3})));
        assertEquals("Invalid signature marker: 1", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource
    void testWithSerializable(final NormalizedNodeStreamVersion version) {
        var input = TestModel.createTestContainer();
        var serializable = new SampleNormalizedNodeSerializable(version, input);
        var clone = clone(serializable);
        assertEquals(input, clone.getInput());
    }

    static List<Arguments> testWithSerializable() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM));
    }

    @ParameterizedTest
    @MethodSource
    void testAnyXmlStreaming(final NormalizedNodeStreamVersion version, final int size) throws Exception {
        var xml = "<foo xmlns=\"http://www.w3.org/TR/html4/\" x=\"123\"><bar>one</bar><bar>two</bar></foo>";
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        final var xmlNode = factory.newDocumentBuilder().parse(
                new InputSource(new StringReader(xml))).getDocumentElement();

        assertEquals("http://www.w3.org/TR/html4/", xmlNode.getNamespaceURI());

        final var anyXmlContainer = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newAnyxmlBuilder(DOMSource.class)
                .withNodeIdentifier(new NodeIdentifier(TestModel.ANY_XML_QNAME))
                .withValue(new DOMSource(xmlNode))
                .build())
            .build();

        final var bos = new ByteArrayOutputStream();
        final var nnout = version.newDataOutput(ByteStreams.newDataOutput(bos));

        nnout.writeNormalizedNode(anyXmlContainer);

        final byte[] bytes = bos.toByteArray();
        assertEquals(size, bytes.length);

        final var nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));

        final var deserialized = (ContainerNode)nnin.readNormalizedNode();

        final var child = deserialized.findChildByArg(new NodeIdentifier(TestModel.ANY_XML_QNAME));
        assertEquals("AnyXml child present", true, child.isPresent());

        final var xmlOutput = new StreamResult(new StringWriter());
        final var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(((DOMSourceAnyxmlNode)child.orElseThrow()).body(), xmlOutput);

        assertEquals("XML", xml, xmlOutput.getWriter().toString());
        assertEquals("http://www.w3.org/TR/html4/",
            ((DOMSourceAnyxmlNode)child.orElseThrow()).body().getNode().getNamespaceURI());
    }

    static List<Arguments> testAnyXmlStreaming() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 229));
    }

    @ParameterizedTest
    @MethodSource
    void testSchemaPathSerialization(final NormalizedNodeStreamVersion version, final int size) throws Exception {
        final var expected = Absolute.of(TestModel.ANY_XML_QNAME);

        final var bos = new ByteArrayOutputStream();
        try (var nnout = version.newDataOutput(ByteStreams.newDataOutput(bos))) {
            nnout.writeSchemaNodeIdentifier(expected);
        }

        final byte[] bytes = bos.toByteArray();
        assertEquals(size, bytes.length);

        var nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(expected, nnin.readSchemaNodeIdentifier());
    }

    static List<Arguments> testSchemaPathSerialization() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 99));
    }

    @ParameterizedTest
    @MethodSource
    void testWritePathArgument(final NormalizedNodeStreamVersion version, final int size) throws Exception {
        final var expected = new NodeIdentifier(TestModel.BOOLEAN_LEAF_QNAME);
        final var bos = new ByteArrayOutputStream();
        try (var nnout = version.newDataOutput(ByteStreams.newDataOutput(bos))) {
            nnout.writePathArgument(expected);
        }

        final byte[] bytes = bos.toByteArray();
        assertEquals(size, bytes.length);

        final var nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(expected, nnin.readPathArgument());
    }

    static List<Arguments> testWritePathArgument() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 103));
    }

    /*
     * This tests the encoding of a MapNode with a lot of entries, each of them having the key leaf (a string)
     * and an integer.
     */
    @ParameterizedTest
    @MethodSource
    void testHugeEntries(final NormalizedNodeStreamVersion version, final int size) throws Exception {
        final var mapBuilder = ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME));
        final var entryBuilder = ImmutableNodes.newMapEntryBuilder()
            .withChild(ImmutableNodes.leafNode(TestModel.DESC_QNAME, (byte) 42));

        for (int i = 0; i < 100_000; ++i) {
            final String key = "xyzzy" + i;
            mapBuilder.addChild(entryBuilder
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.TEST_QNAME,
                    TestModel.CHILD_NAME_QNAME, key))
                .withChild(ImmutableNodes.leafNode(TestModel.CHILD_NAME_QNAME, key))
                .build());
        }

        final var expected = mapBuilder.build();
        final var bos = new ByteArrayOutputStream();

        try (var nnout = version.newDataOutput(ByteStreams.newDataOutput(bos))) {
            nnout.writeNormalizedNode(expected);
        }

        final byte[] bytes = bos.toByteArray();
        assertEquals(size, bytes.length);

        var nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(expected, nnin.readNormalizedNode());
    }

    static List<Arguments> testHugeEntries() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 2_289_103));
    }

    private static <T extends Serializable> T clone(final T obj) {
        final var baos = new ByteArrayOutputStream(512);
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize object", e);
        }

        final byte[] bytes = baos.toByteArray();
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new AssertionError("Failed to deserialize object", e);
        }
    }

    private static String largeString(final int pow) {
        final var sb = new StringBuilder("X");
        for (int i = 0; i < pow; i++) {
            sb.append(sb);
        }
        return sb.toString();
    }
}
