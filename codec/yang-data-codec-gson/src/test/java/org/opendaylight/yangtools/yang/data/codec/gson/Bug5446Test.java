/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import java.io.StringWriter;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug5446Test {
    private static final QNameModule FOO_MODULE = QNameModule.create(XMLNamespace.of("foo"), Revision.of("2015-11-05"));
    private static final QName ROOT_QNAME = QName.create(FOO_MODULE, "root");
    private static final QName IP_ADDRESS_QNAME = QName.create(FOO_MODULE, "ip-address");
    private EffectiveModelContext schemaContext;

    @Test
    void test() throws Exception {
        schemaContext = YangParserTestUtils.parseYang("""
            module foo {
              yang-version 1;
              namespace "foo";
              prefix "foo";

              revision "2015-11-05" {
              }

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

        final var jsonOutput = normalizedNodeToJsonStreamTransformation(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
            .withChild(ImmutableNodes.leafNode(IP_ADDRESS_QNAME, Base64.getDecoder().decode("fwAAAQ==")))
            .build());

        assertEquals(JsonParser.parseString("""
            {
              "foo:root" : {
                "ip-address" : "fwAAAQ=="
               }
            }"""),
            JsonParser.parseString(jsonOutput));
    }

    private String normalizedNodeToJsonStreamTransformation(final ContainerNode inputStructure) throws Exception {
        final var writer = new StringWriter();
        final var jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext),
            JsonWriterFactory.createJsonWriter(writer, 2));
        try (var nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(inputStructure);
        }

        return writer.toString();
    }
}
