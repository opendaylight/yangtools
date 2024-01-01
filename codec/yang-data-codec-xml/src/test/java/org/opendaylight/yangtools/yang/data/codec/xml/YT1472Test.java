/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1472Test {
    private static final QNameModule RESTCONF_MODULE =
        QNameModule.create(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-restconf"), Revision.of("2017-01-26"));
    private static final YangDataName ERRORS_NAME = new YangDataName(RESTCONF_MODULE, "yang-errors");
    private static final NodeIdentifier ERROR_NID = NodeIdentifier.create(QName.create(RESTCONF_MODULE, "error"));

    private static EffectiveModelContext CONTEXT;
    private static XmlCodecFactory CODEC_FACTORY;
    private static Inference ERRORS_INFERENCE;

    @BeforeAll
    static void beforeAll() {
        CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/yt1472");
        CODEC_FACTORY = XmlCodecFactory.create(CONTEXT);

        final var stack = SchemaInferenceStack.of(CONTEXT);
        stack.enterYangData(ERRORS_NAME);
        stack.enterDataTree(QName.create(RESTCONF_MODULE, "errors"));
        ERRORS_INFERENCE = stack.toInference();
    }

    @Test
    void testErrorsParsing() throws Exception {
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, CODEC_FACTORY, ERRORS_INFERENCE);
        // https://www.rfc-editor.org/rfc/rfc8040#page-78, except path
        xmlParser.parse(UntrustedXML.createXMLStreamReader(new StringReader("""
            <errors xmlns="urn:ietf:params:xml:ns:yang:ietf-restconf">
              <error>
                <error-type>protocol</error-type>
                <error-tag>data-exists</error-tag>
                <error-message>Data already exists; cannot create new resource</error-message>
              </error>
            </errors>""")));
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(NodeIdentifier.create(QName.create(RESTCONF_MODULE, "errors")))
            .withChild(ImmutableNodes.newUnkeyedListBuilder()
                .withNodeIdentifier(ERROR_NID)
                .withChild(ImmutableNodes.newUnkeyedListEntryBuilder()
                    .withNodeIdentifier(ERROR_NID)
                    .withChild(ImmutableNodes.leafNode(QName.create(RESTCONF_MODULE, "error-type"), "protocol"))
                    .withChild(ImmutableNodes.leafNode(QName.create(RESTCONF_MODULE, "error-tag"), "data-exists"))
                    .withChild(ImmutableNodes.leafNode(QName.create(RESTCONF_MODULE, "error-message"),
                        "Data already exists; cannot create new resource"))
                    .build())
                .build())
            .build(), result.getResult().data());
    }
}
