/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug5446Test {
    private static final QNameModule FOO_MODULE = QNameModule.create(XMLNamespace.of("foo"), Revision.of("2015-11-05"));
    private static final QName ROOT_QNAME = QName.create(FOO_MODULE, "root");
    private static final QName IP_ADDRESS_QNAME = QName.create(FOO_MODULE, "ip-address");
    private static final String FOO_YANG = """
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
        }""";
    private EffectiveModelContext schemaContext;

    @Before
    public void init() {
        schemaContext = YangParserTestUtils.parseYang(FOO_YANG);
    }

    @Test
    public void test() throws Exception {
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(
            new StringWriter(),
            Builders.containerBuilder()
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

    private String normalizedNodeToJsonStreamTransformation(final Writer writer, final ContainerNode inputStructure)
            throws IOException {
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext),
            JsonWriterFactory.createJsonWriter(writer, 2));
        try (NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(inputStructure);
        }

        return writer.toString();
    }
}
