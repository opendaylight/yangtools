/*
 * Copyright © 2020 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertFalse;

import java.io.StringWriter;
import java.util.Collection;
import javax.xml.stream.XMLOutputFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xmlunit.builder.DiffBuilder;

@RunWith(Parameterized.class)
public class YT1108Test {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return TestFactories.junitParameters();
    }

    private static final QName IDENT_ONE = QName.create("foo-namespace", "ident-one");
    private static final QName IDENTITYREF_LEAF = QName.create("foo-namespace", "identityref-leaf");
    private static final QName LEAF_CONTAINER = QName.create("foo-namespace", "leaf-container");
    private static final QName UNION_IDENTITYREF_LEAF = QName.create("foo-namespace", "union-identityref-leaf");

    private static EffectiveModelContext MODEL_CONTEXT;

    private final XMLOutputFactory factory;

    public YT1108Test(final String factoryMode, final XMLOutputFactory factory) {
        this.factory = factory;
    }

    @BeforeClass
    public static void beforeClass() {
        MODEL_CONTEXT = YangParserTestUtils.parseYang("""
            module foo {
              namespace "foo-namespace";
              prefix "f";

              identity ident-base;

              identity ident-one {
                base ident-base;
              }

              typedef union-type {
                type union {
                  type uint8;
                  type identityref {
                    base ident-base;
                  }
                }
              }

              container leaf-container {
                leaf union-identityref-leaf {
                  type union-type;
                }
                leaf identityref-leaf {
                  type identityref {
                    base ident-base;
                  }
                }
              }
            }""");
    }

    @AfterClass
    public static void afterClass() {
        MODEL_CONTEXT = null;
    }

    @Test
    public void testLeafOfIdentityRefTypeNNToXmlSerialization() throws Exception {
        final var diff = DiffBuilder
            .compare(serializeToXml(Builders.containerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(LEAF_CONTAINER))
                .withChild(Builders.leafBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(IDENTITYREF_LEAF))
                    .withValue(IDENT_ONE)
                    .build())
                .build()))
            .withTest("""
                <?xml version="1.0" encoding="UTF-8"?>

                <leaf-container xmlns="foo-namespace">
                    <identityref-leaf xmlns:prefix="foo-namespace">ident-one</identityref-leaf>
                </leaf-container>""")
            .ignoreWhitespace()
            .checkForIdentical()
            .build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testLeafOfUnionWithIdentityRefNNToXmlSerialization() throws Exception {
        final var diff = DiffBuilder
            .compare(serializeToXml(Builders.containerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(LEAF_CONTAINER))
                .withChild(Builders.leafBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(UNION_IDENTITYREF_LEAF))
                    .withValue(IDENT_ONE)
                    .build())
                .build()))
            .withTest("""
                <?xml version="1.0" encoding="UTF-8"?>

                <leaf-container xmlns="foo-namespace">
                    <union-identityref-leaf xmlns:prefix="foo-namespace">ident-one</union-identityref-leaf>
                </leaf-container>""")
            .ignoreWhitespace()
            .checkForIdentical()
            .build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    private String serializeToXml(final ContainerNode normalizedNode) throws Exception {
        final var sw = new StringWriter();
        try (var nnsw = XMLStreamNormalizedNodeStreamWriter.create(factory.createXMLStreamWriter(sw), MODEL_CONTEXT)) {
            NormalizedNodeWriter.forStreamWriter(nnsw).write(normalizedNode);
        }
        return sw.toString();
    }
}
