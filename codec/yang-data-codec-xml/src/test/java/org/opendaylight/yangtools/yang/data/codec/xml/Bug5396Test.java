/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug5396Test {
    private static final QNameModule FOO = QNameModule.of("foo", "2016-03-22");

    private final EffectiveModelContext schemaContext = YangParserTestUtils.parseYang("""
        module foo {
          yang-version 1;
          namespace "foo";
          prefix "foo";

          revision "2016-03-22" {
            description "test";
          }

          container root {
            leaf my-leaf {
              type my-type;
            }
          }

          typedef my-type {
            type union {
              type string {
                pattern "dp[0-9]+o[0-9]+";
              }
              type string {
                pattern "dp[0-9]+s[0-9]+(f[0-9]+)?(d[0-9]+)?";
              }
              type string {
                pattern "dp[0-9]+(P[0-9]+)?p[0-9]{1,3}s[0-9]{1,3}(f[0-9]+)?(d[0-9]+)?";
              }
              type string {
                pattern "dp[0-9]+p[0-9]+p[0-9]+";
              }
            }
          }
        }""");

    @Test
    void test() throws Exception {
        testInputXML("/bug5396/xml/foo.xml", "dp1o34");
        testInputXML("/bug5396/xml/foo2.xml", "dp0s3f9");
        testInputXML("/bug5396/xml/foo3.xml", "dp09P1p2s3");
        testInputXML("/bug5396/xml/foo4.xml", "dp0p3p1");
        testInputXML("/bug5396/xml/foo5.xml", "dp0s3");

        final var ex = assertThrows(IllegalArgumentException.class,
            () -> testInputXML("/bug5396/xml/invalid-foo.xml", null));
        assertThat(ex.getMessage(), startsWith("Invalid value \"dp09P1p2s1234\" for union type."));
    }

    private void testInputXML(final String xmlPath, final String expectedValue) throws Exception {
        final var reader = UntrustedXML.createXMLStreamReader(
            XmlToNormalizedNodesTest.class.getResourceAsStream(xmlPath));
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "root")));
        xmlParser.parse(reader);

        final var rootContainer = assertInstanceOf(ContainerNode.class, result.getResult().data());
        final var myLeaf = assertInstanceOf(LeafNode.class,
            rootContainer.childByArg(new NodeIdentifier(QName.create(FOO, "my-leaf"))));
        assertEquals(expectedValue, myLeaf.body());
    }
}
