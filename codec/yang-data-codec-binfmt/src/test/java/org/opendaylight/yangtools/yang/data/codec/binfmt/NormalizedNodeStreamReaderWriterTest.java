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

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

@RunWith(Parameterized.class)
public class NormalizedNodeStreamReaderWriterTest {
    public enum Unsigned implements Function<String, Number> {
        BIG_INTEGER {
            @Override
            public BigInteger apply(final String str) {
                return new BigInteger(str);
            }
        },
        UINT64 {
            @Override
            public Uint64 apply(final String str) {
                return Uint64.valueOf(str);
            }
        };
    }

    @Parameters(name = "{0} {1}")
    public static Iterable<Object[]> data() {
        return List.of(
            new Object[] { NormalizedNodeStreamVersion.LITHIUM,    Unsigned.BIG_INTEGER,
                1_050_286, 9_577_973, 171, 1_553, 103, 237,  98 },
            new Object[] { NormalizedNodeStreamVersion.NEON_SR2,   Unsigned.BIG_INTEGER,
                1_049_950, 5_577_993, 161, 1_163, 105, 235, 100 },
            new Object[] { NormalizedNodeStreamVersion.SODIUM_SR1, Unsigned.BIG_INTEGER,
                1_049_619, 2_289_103, 139,   826, 103, 229,  99 },
            new Object[] { NormalizedNodeStreamVersion.SODIUM_SR1, Unsigned.UINT64,
                1_049_618, 2_289_103, 139,   825, 103, 229,  99 },
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM,  Unsigned.UINT64,
                1_049_618, 2_289_103, 139,   825, 103, 229,  99 });
    }

    @Parameter(0)
    public NormalizedNodeStreamVersion version;
    @Parameter(1)
    public Unsigned uint64;
    @Parameter(2)
    public int normalizedNodeStreamingSize;
    @Parameter(3)
    public int hugeEntriesSize;
    @Parameter(4)
    public int yiidStreamingSize;
    @Parameter(5)
    public int nnYiidStreamingSize;
    @Parameter(6)
    public int writePathArgumentSize;
    @Parameter(7)
    public int anyxmlStreamingSize;
    @Parameter(8)
    public int schemaPathSize;

    @Test
    public void testNormalizedNodeStreaming() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(bos));

        ContainerNode testContainer = createTestContainer();
        nnout.writeNormalizedNode(testContainer);

        QName toaster = QName.create("http://netconfcentral.org/ns/toaster","2009-11-20","toaster");
        QName darknessFactor = QName.create("http://netconfcentral.org/ns/toaster","2009-11-20","darknessFactor");
        QName description = QName.create("http://netconfcentral.org/ns/toaster","2009-11-20","description");
        ContainerNode toasterNode = Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(toaster))
                .withChild(ImmutableNodes.leafNode(darknessFactor, "1000"))
                .withChild(ImmutableNodes.leafNode(description, largeString(20))).build();

        ContainerNode toasterContainer = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME)).withChild(toasterNode).build();
        nnout.writeNormalizedNode(toasterContainer);

        final byte[] bytes = bos.toByteArray();
        assertEquals(normalizedNodeStreamingSize, bytes.length);

        NormalizedNodeDataInput nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));

        assertEquals(testContainer, nnin.readNormalizedNode());
        assertEquals(toasterContainer, nnin.readNormalizedNode());
    }

    private ContainerNode createTestContainer() {
        final byte[] bytes1 = {1, 2, 3};
        final byte[] bytes2 = {};

        return TestModel.createBaseTestContainerBuilder(uint64)
            .withChild(Builders.leafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.BINARY_LEAF_LIST_QNAME))
                .withChild(Builders.leafSetEntryBuilder()
                    .withNodeIdentifier(new NodeWithValue<>(TestModel.BINARY_LEAF_LIST_QNAME, bytes1))
                    .withValue(bytes1)
                    .build())
                .withChild(Builders.leafSetEntryBuilder()
                    .withNodeIdentifier(new NodeWithValue<>(TestModel.BINARY_LEAF_LIST_QNAME, bytes2))
                    .withValue(bytes2)
                    .build())
                .build())
            .withChild(ImmutableNodes.leafNode(TestModel.SOME_BINARY_DATA_QNAME, new byte[]{1, 2, 3, 4}))
            .withChild(ImmutableNodes.leafNode(TestModel.EMPTY_QNAME, Empty.value()))
            .withChild(Builders.orderedMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.ORDERED_LIST_QNAME))
                .withChild(ImmutableNodes.mapEntry(TestModel.ORDERED_LIST_ENTRY_QNAME, TestModel.ID_QNAME, 11))
                .build())
            .build();
    }

    @Test
    public void testYangInstanceIdentifierStreaming() throws IOException  {
        YangInstanceIdentifier path = YangInstanceIdentifier.builder(TestModel.TEST_PATH)
                .node(TestModel.OUTER_LIST_QNAME).nodeWithKey(
                        TestModel.INNER_LIST_QNAME, TestModel.ID_QNAME, 10).build();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(bos));

        nnout.writeYangInstanceIdentifier(path);

        final byte[] bytes = bos.toByteArray();
        assertEquals(yiidStreamingSize, bytes.length);

        NormalizedNodeDataInput nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(path, nnin.readYangInstanceIdentifier());
    }

    @Test
    public void testNormalizedNodeAndYangInstanceIdentifierStreaming() throws IOException {
        final NormalizedNode testContainer = TestModel.createBaseTestContainerBuilder(uint64).build();
        final YangInstanceIdentifier path = YangInstanceIdentifier.builder(TestModel.TEST_PATH)
            .node(TestModel.OUTER_LIST_QNAME)
            .nodeWithKey(TestModel.INNER_LIST_QNAME, TestModel.ID_QNAME, 10)
            .build();

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (NormalizedNodeDataOutput writer = version.newDataOutput(ByteStreams.newDataOutput(bos))) {
            writer.writeNormalizedNode(testContainer);
            writer.writeYangInstanceIdentifier(path);
        }

        final byte[] bytes = bos.toByteArray();
        assertEquals(nnYiidStreamingSize, bytes.length);

        final NormalizedNodeDataInput reader = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(testContainer, reader.readNormalizedNode());
        assertEquals(path, reader.readYangInstanceIdentifier());
    }

    @Test
    public void testInvalidNormalizedNodeStream() throws IOException {
        final InvalidNormalizedNodeStreamException ex = assertThrows(InvalidNormalizedNodeStreamException.class,
            () -> NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(new byte[] { 1, 2, 3})));
        assertEquals("Invalid signature marker: 1", ex.getMessage());
    }

    @Test
    public void testWithSerializable() {
        NormalizedNode input = TestModel.createTestContainer(uint64);
        SampleNormalizedNodeSerializable serializable = new SampleNormalizedNodeSerializable(version, input);
        SampleNormalizedNodeSerializable clone = clone(serializable);
        assertEquals(input, clone.getInput());
    }

    @Test
    public void testAnyXmlStreaming() throws Exception {
        String xml = "<foo xmlns=\"http://www.w3.org/TR/html4/\" x=\"123\"><bar>one</bar><bar>two</bar></foo>";
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        Node xmlNode = factory.newDocumentBuilder().parse(
                new InputSource(new StringReader(xml))).getDocumentElement();

        assertEquals("http://www.w3.org/TR/html4/", xmlNode.getNamespaceURI());

        ContainerNode anyXmlContainer = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(Builders.anyXmlBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.ANY_XML_QNAME))
                .withValue(new DOMSource(xmlNode))
                .build())
            .build();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(bos));

        nnout.writeNormalizedNode(anyXmlContainer);

        final byte[] bytes = bos.toByteArray();
        assertEquals(anyxmlStreamingSize, bytes.length);

        NormalizedNodeDataInput nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));

        ContainerNode deserialized = (ContainerNode)nnin.readNormalizedNode();

        Optional<DataContainerChild> child = deserialized.findChildByArg(new NodeIdentifier(TestModel.ANY_XML_QNAME));
        assertEquals("AnyXml child present", true, child.isPresent());

        StreamResult xmlOutput = new StreamResult(new StringWriter());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(((DOMSourceAnyxmlNode)child.get()).body(), xmlOutput);

        assertEquals("XML", xml, xmlOutput.getWriter().toString());
        assertEquals("http://www.w3.org/TR/html4/",
            ((DOMSourceAnyxmlNode)child.get()).body().getNode().getNamespaceURI());
    }

    @Test
    public void testSchemaPathSerialization() throws IOException {
        final Absolute expected = Absolute.of(TestModel.ANY_XML_QNAME);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(bos))) {
            nnout.writeSchemaNodeIdentifier(expected);
        }

        final byte[] bytes = bos.toByteArray();
        assertEquals(schemaPathSize, bytes.length);

        NormalizedNodeDataInput nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(expected, nnin.readSchemaNodeIdentifier());
    }

    @Test
    public void testWritePathArgument() throws IOException {
        final NodeIdentifier expected = new NodeIdentifier(TestModel.BOOLEAN_LEAF_QNAME);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(bos))) {
            nnout.writePathArgument(expected);
        }

        final byte[] bytes = bos.toByteArray();
        assertEquals(writePathArgumentSize, bytes.length);

        NormalizedNodeDataInput nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(expected, nnin.readPathArgument());
    }

    /*
     * This tests the encoding of a MapNode with a lot of entries, each of them having the key leaf (a string)
     * and an integer.
     */
    @Test
    public void testHugeEntries() throws IOException {
        final var mapBuilder = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME));
        final var entryBuilder = Builders.mapEntryBuilder()
            .withChild(ImmutableNodes.leafNode(TestModel.DESC_QNAME, (byte) 42));

        for (int i = 0; i < 100_000; ++i) {
            final String key = "xyzzy" + i;
            mapBuilder.addChild(entryBuilder
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.TEST_QNAME,
                    TestModel.CHILD_NAME_QNAME, key))
                .withChild(ImmutableNodes.leafNode(TestModel.CHILD_NAME_QNAME, key))
                .build());
        }

        final SystemMapNode expected = mapBuilder.build();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(bos))) {
            nnout.writeNormalizedNode(expected);
        }

        final byte[] bytes = bos.toByteArray();
        assertEquals(hugeEntriesSize, bytes.length);

        NormalizedNodeDataInput nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        assertEquals(expected, nnin.readNormalizedNode());
    }

    @Test
    public void testAugmentationIdentifier() throws IOException {
        final List<QName> qnames = new ArrayList<>();
        for (int i = 0; i < 257; ++i) {
            qnames.add(QName.create(TestModel.TEST_QNAME, "a" + Integer.toHexString(i)));
        }

        for (int i = 0; i < qnames.size(); ++i) {
            assertAugmentationIdentifier(AugmentationIdentifier.create(ImmutableSet.copyOf(qnames.subList(0, i))));
        }

        for (int i = qnames.size(); i < 65536; ++i) {
            qnames.add(QName.create(TestModel.TEST_QNAME, "a" + Integer.toHexString(i)));
        }
        assertAugmentationIdentifier(AugmentationIdentifier.create(ImmutableSet.copyOf(qnames)));
    }

    private void assertAugmentationIdentifier(final AugmentationIdentifier expected) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(bos))) {
            nnout.writePathArgument(expected);
        }

        final byte[] bytes = bos.toByteArray();

        NormalizedNodeDataInput nnin = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
        PathArgument arg = nnin.readPathArgument();
        assertEquals(expected, arg);
    }

    private static <T extends Serializable> T clone(final T obj) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize object", e);
        }

        final byte[] bytes = baos.toByteArray();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new AssertionError("Failed to deserialize object", e);
        }
    }

    private static String largeString(final int pow) {
        StringBuilder sb = new StringBuilder("X");
        for (int i = 0; i < pow; i++) {
            sb.append(sb);
        }
        return sb.toString();
    }
}
