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
import java.util.Base64;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xmlunit.builder.DiffBuilder;

public class Bug5446Test {
    @Test
    public void test() throws Exception {
        final var modelContext = YangParserTestUtils.parseYang("""
            module foo {
              yang-version 1;
              namespace "foo";
              prefix "foo";

              revision "2015-11-05"

              typedef ipv4-address-binary {
                type binary {
                  length "4";
                }
              }

              typedef ipv6-address-binary {
                type binary {
                  length "16";
                }
              }

              typedef ip-address-binary {
                type union {
                  type ipv4-address-binary;
                  type ipv6-address-binary;
                }
              }

              container root {
                leaf ip-address {
                  type ip-address-binary;
                }
              }
            }""");

        final var decoded = Base64.getDecoder().decode("fwAAAQ==");
        assertEquals("fwAAAQ==", Base64.getEncoder().encodeToString(decoded));

        final var sw = new StringWriter();
        try (var nnsw = XMLStreamNormalizedNodeStreamWriter.create(
                TestFactories.DEFAULT_OUTPUT_FACTORY.createXMLStreamWriter(sw), modelContext)) {
            NormalizedNodeWriter.forStreamWriter(nnsw)
                .write(Builders.containerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(QName.create("foo", "2015-11-05", "root")))
                    .withChild(ImmutableNodes.leafNode(QName.create("foo", "2015-11-05", "ip-address"), decoded))
                    .build())
                .flush();
        }

        final var diff = DiffBuilder.compare(sw.toString())
            .withTest("""
            <?xml version="1.0" encoding="UTF-8"?>
            <root xmlns="foo">
                <ip-address>fwAAAQ==</ip-address>
            </root>""")
            .ignoreWhitespace()
            .checkForIdentical()
            .build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }
}
