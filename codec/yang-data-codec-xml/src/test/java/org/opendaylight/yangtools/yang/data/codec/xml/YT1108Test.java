/*
 * Copyright © 2020 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xmlunit.builder.DiffBuilder;

class YT1108Test {
    private static final QName IDENT_ONE = QName.create("foo-namespace", "ident-one");
    private static final QName IDENTITYREF_LEAF = QName.create("foo-namespace", "identityref-leaf");
    private static final QName LEAF_CONTAINER = QName.create("foo-namespace", "leaf-container");
    private static final QName UNION_IDENTITYREF_LEAF = QName.create("foo-namespace", "union-identityref-leaf");

    private static EffectiveModelContext MODEL_CONTEXT;

    @BeforeAll
    static void beforeClass() {
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

    @AfterAll
    static void afterClass() {
        MODEL_CONTEXT = null;
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    void testLeafOfIdentityRefTypeNNToXmlSerialization(final String factoryMode, final XMLOutputFactory factory)
            throws Exception {
        final var diff = DiffBuilder
            .compare(serializeToXml(factory, ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(LEAF_CONTAINER))
                .withChild(ImmutableNodes.leafNode(IDENTITYREF_LEAF, IDENT_ONE))
                .build()))
            .withTest("""
                <?xml version="1.0" encoding="UTF-8"?>

                <leaf-container xmlns="foo-namespace">
                    <identityref-leaf xmlns:prefix="foo-namespace">ident-one</identityref-leaf>
                </leaf-container>""")
            .ignoreWhitespace()
            .checkForIdentical()
            .build();
        assertFalse(diff.hasDifferences(), diff.toString());
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    void testLeafOfUnionWithIdentityRefNNToXmlSerialization(final String factoryMode, final XMLOutputFactory factory)
            throws Exception {
        final var diff = DiffBuilder
            .compare(serializeToXml(factory, ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(LEAF_CONTAINER))
                .withChild(ImmutableNodes.leafNode(UNION_IDENTITYREF_LEAF, IDENT_ONE))
                .build()))
            .withTest("""
                <?xml version="1.0" encoding="UTF-8"?>

                <leaf-container xmlns="foo-namespace">
                    <union-identityref-leaf xmlns:prefix="foo-namespace">ident-one</union-identityref-leaf>
                </leaf-container>""")
            .ignoreWhitespace()
            .checkForIdentical()
            .build();
        assertFalse(diff.hasDifferences(), diff.toString());
    }

    private static String serializeToXml(final XMLOutputFactory factory, final ContainerNode normalizedNode)
            throws Exception {
        final var sw = new StringWriter();
        try (var nnsw = XMLStreamNormalizedNodeStreamWriter.create(factory.createXMLStreamWriter(sw), MODEL_CONTEXT)) {
            NormalizedNodeWriter.forStreamWriter(nnsw).write(normalizedNode);
        }
        return sw.toString();
    }
}
