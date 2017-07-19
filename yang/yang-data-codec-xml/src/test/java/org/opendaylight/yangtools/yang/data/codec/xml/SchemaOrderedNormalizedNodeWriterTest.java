/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaOrderedNormalizedNodeWriter;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class SchemaOrderedNormalizedNodeWriterTest {

    private static final String EXPECTED_1 =
            "<root>\n" +
            "    <policy>\n" +
            "        <name>policy1</name>\n" +
            "        <rule>\n" +
            "            <name>rule1</name>\n" +
            "        </rule>\n" +
            "        <rule>\n" +
            "            <name>rule2</name>\n" +
            "        </rule>\n" +
            "        <rule>\n" +
            "            <name>rule3</name>\n" +
            "        </rule>\n" +
            "        <rule>\n" +
            "            <name>rule4</name>\n" +
            "        </rule>\n" +
            "    </policy>\n" +
            "    <policy>\n" +
            "        <name>policy2</name>\n" +
            "    </policy>\n" +
            "</root>\n";


    private static final String EXPECTED_2 = "<root>\n" +
            "    <id>id1</id>\n" +
            "    <cont>\n" +
            "        <content>content1</content>\n" +
            "    </cont>\n" +
            "</root>";

    private static final String FOO_NAMESPACE = "foo";
    private static final String RULE_NODE = "rule";
    private static final String NAME_NODE = "name";
    private static final String POLICY_NODE = "policy";
    private static final String ORDER_NAMESPACE = "order";


    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testWrite() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(stringWriter);

        SchemaContext schemaContext = getSchemaContext("/bug1848/foo.yang");
        NormalizedNodeStreamWriter writer = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, schemaContext);

        try (SchemaOrderedNormalizedNodeWriter nnw = new SchemaOrderedNormalizedNodeWriter(writer, schemaContext,
            SchemaPath.ROOT)) {

            List<MapEntryNode> rule1Names = new ArrayList<>();
            rule1Names.add(ImmutableNodes.mapEntry(createQName(FOO_NAMESPACE, RULE_NODE),
                createQName(FOO_NAMESPACE, NAME_NODE), "rule1"));
            rule1Names.add(ImmutableNodes.mapEntry(createQName(FOO_NAMESPACE, RULE_NODE),
                createQName(FOO_NAMESPACE, NAME_NODE), "rule2"));

            List<MapEntryNode> rule2Names = new ArrayList<>();
            rule1Names.add(ImmutableNodes.mapEntry(createQName(FOO_NAMESPACE, RULE_NODE),
                createQName(FOO_NAMESPACE, NAME_NODE), "rule3"));
            rule1Names.add(ImmutableNodes.mapEntry(createQName(FOO_NAMESPACE, RULE_NODE),
                createQName(FOO_NAMESPACE, NAME_NODE), "rule4"));

            DataContainerChild<?, ?> rules1 = Builders.orderedMapBuilder()
                    .withNodeIdentifier(getNodeIdentifier(FOO_NAMESPACE, RULE_NODE))
                    .withValue(rule1Names)
                    .build();
            DataContainerChild<?, ?> rules2 = Builders.orderedMapBuilder()
                    .withNodeIdentifier(getNodeIdentifier(FOO_NAMESPACE, RULE_NODE))
                    .withValue(rule2Names)
                    .build();

            List<MapEntryNode> policyNodes = new ArrayList<>();


            final MapEntryNode pn1 = ImmutableNodes
                    .mapEntryBuilder(createQName(FOO_NAMESPACE, POLICY_NODE),
                        createQName(FOO_NAMESPACE, NAME_NODE), "policy1")
                    .withChild(rules1)
                    .build();
            final MapEntryNode pn2 = ImmutableNodes
                    .mapEntryBuilder(createQName(FOO_NAMESPACE, POLICY_NODE),
                        createQName(FOO_NAMESPACE, NAME_NODE), "policy2")
                    .withChild(rules2)
                    .build();
            policyNodes.add(pn1);
            policyNodes.add(pn2);

            DataContainerChild<?, ?> policy = Builders.orderedMapBuilder()
                    .withNodeIdentifier(getNodeIdentifier(FOO_NAMESPACE, POLICY_NODE))
                    .withValue(policyNodes)
                    .build();
            NormalizedNode<?, ?> root = Builders.containerBuilder()
                    .withNodeIdentifier(getNodeIdentifier(FOO_NAMESPACE, "root"))
                    .withChild(policy).build();
            nnw.write(root);
        }

        XMLAssert.assertXMLIdentical(new Diff(EXPECTED_1, stringWriter.toString()), true);
    }

    @Test
    public void testWriteOrder() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(stringWriter);
        SchemaContext schemaContext = getSchemaContext("/bug1848/order.yang");
        NormalizedNodeStreamWriter writer = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, schemaContext);

        try (NormalizedNodeWriter nnw = new SchemaOrderedNormalizedNodeWriter(writer, schemaContext, SchemaPath.ROOT)) {

            DataContainerChild<?, ?> cont = Builders.containerBuilder()
                    .withNodeIdentifier(getNodeIdentifier(ORDER_NAMESPACE, "cont"))
                    .withChild(ImmutableNodes.leafNode(createQName(ORDER_NAMESPACE, "content"), "content1"))
                    .build();

            NormalizedNode<?, ?> root = Builders.containerBuilder()
                    .withNodeIdentifier(getNodeIdentifier(ORDER_NAMESPACE, "root"))
                    .withChild(cont)
                    .withChild(ImmutableNodes.leafNode(createQName(ORDER_NAMESPACE, "id"), "id1"))
                    .build();

            nnw.write(root);
        }

        XMLAssert.assertXMLIdentical(new Diff(EXPECTED_2, stringWriter.toString()), true);
    }

    private static SchemaContext getSchemaContext(final String filePath) throws URISyntaxException,
            ReactorException, FileNotFoundException {
        return YangParserTestUtils.parseYangSource(filePath);
    }

    private static YangInstanceIdentifier.NodeIdentifier getNodeIdentifier(final String ns, final String name) {
        return YangInstanceIdentifier.NodeIdentifier.create(createQName(ns, name));
    }

    private static QName createQName(final String ns, final String name) {
        return QName.create(ns, "2016-02-17", name);
    }

}