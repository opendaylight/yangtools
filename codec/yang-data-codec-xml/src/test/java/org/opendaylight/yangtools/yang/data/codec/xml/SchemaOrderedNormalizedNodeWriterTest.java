/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaOrderedNormalizedNodeWriter;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xmlunit.builder.DiffBuilder;

@RunWith(Parameterized.class)
public class SchemaOrderedNormalizedNodeWriterTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return TestFactories.junitParameters();
    }

    private static final String FOO_NAMESPACE = "foo";
    private static final String RULE_NODE = "rule";
    private static final String NAME_NODE = "name";
    private static final String POLICY_NODE = "policy";
    private static final String ORDER_NAMESPACE = "order";

    private final XMLOutputFactory factory;

    public SchemaOrderedNormalizedNodeWriterTest(final String factoryMode, final XMLOutputFactory factory) {
        this.factory = factory;
    }

    @Test
    public void testWrite() throws Exception {
        final var stringWriter = new StringWriter();
        final var xmlStreamWriter = factory.createXMLStreamWriter(stringWriter);

        final var schemaContext = YangParserTestUtils.parseYang("""
            module foo {
              namespace "foo";
              prefix "foo";
              revision "2016-02-17";

              container root {
                list policy {
                  key name;
                  leaf name {
                    type string;
                  }
                  list rule {
                    key name;
                    leaf name {
                      type string;
                    }
                  }
                }
              }
            }""");
        var writer = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, schemaContext);

        try (var nnw = new SchemaOrderedNormalizedNodeWriter(writer, schemaContext)) {

            final var rule1Names = new ArrayList<MapEntryNode>();
            rule1Names.add(ImmutableNodes.mapEntry(createQName(FOO_NAMESPACE, RULE_NODE),
                createQName(FOO_NAMESPACE, NAME_NODE), "rule1"));
            rule1Names.add(ImmutableNodes.mapEntry(createQName(FOO_NAMESPACE, RULE_NODE),
                createQName(FOO_NAMESPACE, NAME_NODE), "rule2"));

            final var rule2Names = new ArrayList<MapEntryNode>();
            rule1Names.add(ImmutableNodes.mapEntry(createQName(FOO_NAMESPACE, RULE_NODE),
                createQName(FOO_NAMESPACE, NAME_NODE), "rule3"));
            rule1Names.add(ImmutableNodes.mapEntry(createQName(FOO_NAMESPACE, RULE_NODE),
                createQName(FOO_NAMESPACE, NAME_NODE), "rule4"));

            final var rules1 = Builders.orderedMapBuilder()
                    .withNodeIdentifier(getNodeIdentifier(FOO_NAMESPACE, RULE_NODE))
                    .withValue(rule1Names)
                    .build();
            final var rules2 = Builders.orderedMapBuilder()
                    .withNodeIdentifier(getNodeIdentifier(FOO_NAMESPACE, RULE_NODE))
                    .withValue(rule2Names)
                    .build();

            final var policyNodes = new ArrayList<MapEntryNode>();


            final var pn1 = ImmutableNodes.mapEntryBuilder(createQName(FOO_NAMESPACE, POLICY_NODE),
                        createQName(FOO_NAMESPACE, NAME_NODE), "policy1")
                    .withChild(rules1)
                    .build();
            final var pn2 = ImmutableNodes.mapEntryBuilder(createQName(FOO_NAMESPACE, POLICY_NODE),
                        createQName(FOO_NAMESPACE, NAME_NODE), "policy2")
                    .withChild(rules2)
                    .build();
            policyNodes.add(pn1);
            policyNodes.add(pn2);

            final var policy = Builders.orderedMapBuilder()
                    .withNodeIdentifier(getNodeIdentifier(FOO_NAMESPACE, POLICY_NODE))
                    .withValue(policyNodes)
                    .build();
            final var root = Builders.containerBuilder()
                    .withNodeIdentifier(getNodeIdentifier(FOO_NAMESPACE, "root"))
                    .withChild(policy).build();
            nnw.write(root);
        }

        final var diff = DiffBuilder.compare(stringWriter.toString())
            .withTest("""
                <root xmlns="foo">
                    <policy>
                        <name>policy1</name>
                        <rule>
                            <name>rule1</name>
                        </rule>
                        <rule>
                            <name>rule2</name>
                        </rule>
                        <rule>
                            <name>rule3</name>
                        </rule>
                        <rule>
                            <name>rule4</name>
                        </rule>
                    </policy>
                    <policy>
                        <name>policy2</name>
                    </policy>
                </root>""")
            .ignoreWhitespace()
            .checkForIdentical()
            .build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testWriteOrder() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(stringWriter);
        EffectiveModelContext schemaContext = YangParserTestUtils.parseYang("""
            module order {
              namespace "order";
              prefix "order";
              revision "2016-02-17";

              container root {
                leaf id {
                  type string;
                }
                container cont {
                  leaf content {
                    type string;
                  }
                }
              }
            }""");
        var writer = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, schemaContext);

        try (var nnw = new SchemaOrderedNormalizedNodeWriter(writer, schemaContext)) {

            final var cont = Builders.containerBuilder()
                    .withNodeIdentifier(getNodeIdentifier(ORDER_NAMESPACE, "cont"))
                    .withChild(ImmutableNodes.leafNode(createQName(ORDER_NAMESPACE, "content"), "content1"))
                    .build();

            final var root = Builders.containerBuilder()
                    .withNodeIdentifier(getNodeIdentifier(ORDER_NAMESPACE, "root"))
                    .withChild(cont)
                    .withChild(ImmutableNodes.leafNode(createQName(ORDER_NAMESPACE, "id"), "id1"))
                    .build();

            nnw.write(root);
        }

        assertEquals("""
            <root xmlns="order"><id>id1</id><cont><content>content1</content></cont></root>""",
            stringWriter.toString());
    }

    private static NodeIdentifier getNodeIdentifier(final String ns, final String name) {
        return NodeIdentifier.create(createQName(ns, name));
    }

    private static QName createQName(final String ns, final String name) {
        return QName.create(ns, "2016-02-17", name);
    }
}
