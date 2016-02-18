/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.io.File;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XMLStreamNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

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


    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testWrite() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(stringWriter);

        SchemaContext schemaContext = getSchemaContext("/bug-1848.yang");
        NormalizedNodeStreamWriter writer = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, schemaContext);
        SchemaOrderedNormalizedNodeWriter nnw = new SchemaOrderedNormalizedNodeWriter(writer, schemaContext, SchemaPath.ROOT);

        List<MapEntryNode> rule1Names = new ArrayList<>();
        rule1Names.add(ImmutableNodes.mapEntry(createQName("foo", "rule"), createQName("foo", "name"), "rule1"));
        rule1Names.add(ImmutableNodes.mapEntry(createQName("foo", "rule"), createQName("foo", "name"), "rule2"));

        List<MapEntryNode> rule2Names = new ArrayList<>();
        rule1Names.add(ImmutableNodes.mapEntry(createQName("foo", "rule"), createQName("foo", "name"), "rule3"));
        rule1Names.add(ImmutableNodes.mapEntry(createQName("foo", "rule"), createQName("foo", "name"), "rule4"));

        DataContainerChild<?, ?> rules1 = Builders.orderedMapBuilder()
                .withNodeIdentifier(getNodeIdentifier("foo", "rule"))
                .withValue(rule1Names)
                .build();
        DataContainerChild<?, ?> rules2 = Builders.orderedMapBuilder()
                .withNodeIdentifier(getNodeIdentifier("foo", "rule"))
                .withValue(rule2Names)
                .build();

        List<MapEntryNode> policyNodes = new ArrayList<>();


        final MapEntryNode pn1 = ImmutableNodes
                .mapEntryBuilder(createQName("foo", "policy"), createQName("foo", "name"), "policy1")
                .withChild(rules1)
                .build();
        final MapEntryNode pn2 = ImmutableNodes
                .mapEntryBuilder(createQName("foo", "policy"), createQName("foo", "name"), "policy2")
                .withChild(rules2)
                .build();
        policyNodes.add(pn1);
        policyNodes.add(pn2);



        DataContainerChild<?, ?> policy = Builders.orderedMapBuilder()
                .withNodeIdentifier(getNodeIdentifier("foo", "policy"))
                .withValue(policyNodes)
                .build();
        NormalizedNode<?, ?> root = Builders.containerBuilder()
                .withNodeIdentifier(getNodeIdentifier("foo", "root"))
                .withChild(policy).build();
        nnw.write(root);

        XMLAssert.assertXMLIdentical(new Diff(EXPECTED_1, stringWriter.toString()), true);
    }

    @Test
    public void testWriteOrder() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(stringWriter);
        SchemaContext schemaContext = getSchemaContext("/bug-1848-2.yang");
        NormalizedNodeStreamWriter writer = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, schemaContext);

        NormalizedNodeWriter nnw = new SchemaOrderedNormalizedNodeWriter(writer, schemaContext, SchemaPath.ROOT);

        DataContainerChild<?, ?> cont = Builders.containerBuilder()
                .withNodeIdentifier(getNodeIdentifier("order", "cont"))
                .withChild(ImmutableNodes.leafNode(createQName("order", "content"), "content1"))
                .build();

        NormalizedNode<?, ?> root = Builders.containerBuilder()
                .withNodeIdentifier(getNodeIdentifier("order", "root"))
                .withChild(cont)
                .withChild(ImmutableNodes.leafNode(createQName("order", "id"), "id1"))
                .build();

        nnw.write(root);
        System.out.println(stringWriter.toString());
        System.out.println(EXPECTED_2);

        XMLAssert.assertXMLIdentical(new Diff(EXPECTED_2, stringWriter.toString()), true);
    }

    private SchemaContext getSchemaContext(String filePath) throws URISyntaxException {
        File file = new File(getClass().getResource(filePath).toURI());
        return YangParserImpl.getInstance().parseFiles(Collections.singletonList(file));
    }

    private YangInstanceIdentifier.NodeIdentifier getNodeIdentifier(String ns, String name) {
        return YangInstanceIdentifier.NodeIdentifier.create(createQName(ns, name));
    }

    private QName createQName(String ns, String name) {
        return QName.create(ns, "2016-02-17", name);
    }

}