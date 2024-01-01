/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;

class AnydataNormalizeContentTest extends AbstractAnydataTest {
    private static final QName BAR_QNAME = QName.create(FOO_QNAME, "bar"); // container level 2
    private static final QName LIST_QNAME = QName.create(FOO_QNAME, "lst"); // list
    private static final QName LEAF_LIST_QNAME = QName.create(FOO_QNAME, "my-leafs"); // leaf-list of type string
    private static final QName LEAF_EMPTY_QNAME = QName.create(FOO_QNAME, "empty-leaf"); // leaf of type empty
    private static final NodeIdentifier BAR_NODEID = NodeIdentifier.create(BAR_QNAME);
    private static final NodeIdentifier LIST_NODEID = NodeIdentifier.create(LIST_QNAME);
    private static final NodeIdentifier LEAF_LIST_NODEID = NodeIdentifier.create(LEAF_LIST_QNAME);
    private static final NodeIdentifier LEAF_EMPTY_NODEID = NodeIdentifier.create(LEAF_EMPTY_QNAME);
    private static final LeafNode<String> LEAF_NODE = ImmutableNodes.leafNode(CONT_LEAF_NODEID, "test");

    private static final String ANYDATA_XML = "<foo xmlns=\"test-anydata\"><cont-leaf>test</cont-leaf></foo>";
    private static final String ANYDATA_EMPTY_XML = "<foo xmlns=\"test-anydata\" />";

    @ParameterizedTest(name = "Anydata normalize to {0}")
    @MethodSource
    void anydataNormalize(final String testDesc, final String xml, final Inference inference,
            final NormalizedNode expectedData) throws Exception {

        final var reader = UntrustedXML.createXMLStreamReader(toInputStream(xml));
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, Inference.ofDataTreePath(SCHEMA_CONTEXT, FOO_QNAME));
        xmlParser.parse(reader);

        final AnydataNode<?> anydataNode = assertInstanceOf(AnydataNode.class, result.getResult().data());
        final DOMSourceAnydata domSourceAnydata = assertInstanceOf(DOMSourceAnydata.class, anydataNode.body());

        final NormalizedAnydata normalizedAnydata = domSourceAnydata.normalizeTo(inference);
        assertNotNull(normalizedAnydata);
        assertEquals(expectedData, normalizedAnydata.getData());
    }

    private static List<Arguments> anydataNormalize() {
        // test case descriptor, xml, inference, expected normalized data
        return List.of(
            Arguments.of("container (root level)",
                ANYDATA_XML,
                Inference.ofDataTreePath(SCHEMA_CONTEXT, CONT_QNAME),
                ImmutableNodes.newContainerBuilder().withNodeIdentifier(CONT_NODEID).withChild(LEAF_NODE).build()),
            Arguments.of("container (level 2)",
                ANYDATA_XML,
                Inference.ofDataTreePath(SCHEMA_CONTEXT, CONT_QNAME, BAR_QNAME),
                ImmutableNodes.newContainerBuilder().withNodeIdentifier(BAR_NODEID).withChild(LEAF_NODE).build()),
            Arguments.of("empty container",
                ANYDATA_EMPTY_XML,
                Inference.ofDataTreePath(SCHEMA_CONTEXT, CONT_QNAME, BAR_QNAME),
                ImmutableNodes.newContainerBuilder().withNodeIdentifier(BAR_NODEID).build()),
            Arguments.of("single list element",
                ANYDATA_XML,
                Inference.ofDataTreePath(SCHEMA_CONTEXT, LIST_QNAME),
                ImmutableNodes.newUnkeyedListBuilder()
                    .withNodeIdentifier(LIST_NODEID)
                    .withChild(ImmutableNodes.newUnkeyedListEntryBuilder()
                        .withNodeIdentifier(LIST_NODEID)
                        .withChild(LEAF_NODE)
                        .build())
                    .build()),
            Arguments.of("single empty list element",
                ANYDATA_EMPTY_XML,
                Inference.ofDataTreePath(SCHEMA_CONTEXT, LIST_QNAME),
                ImmutableNodes.newUnkeyedListBuilder()
                    .withNodeIdentifier(LIST_NODEID)
                    .withChild(ImmutableNodes.newUnkeyedListEntryBuilder()
                        .withNodeIdentifier(LIST_NODEID)
                        .build())
                    .build()),
            Arguments.of("single empty leaf-list element",
                ANYDATA_EMPTY_XML,
                Inference.ofDataTreePath(SCHEMA_CONTEXT, LIST_QNAME, LEAF_LIST_QNAME),
                ImmutableNodes.newSystemLeafSetBuilder()
                    .withNodeIdentifier(LEAF_LIST_NODEID)
                    .withChild(ImmutableNodes.leafSetEntry(LEAF_LIST_QNAME, ""))
                    .build()),
            Arguments.of("leaf of type empty",
                ANYDATA_EMPTY_XML,
                Inference.ofDataTreePath(SCHEMA_CONTEXT, CONT_QNAME, LEAF_EMPTY_QNAME),
                ImmutableNodes.leafNode(LEAF_EMPTY_NODEID, Empty.value())));
    }
}
