/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
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
    private static JSONCodecFactory CODEC_FACTORY;
    private static Inference ERRORS_INFERENCE;

    @BeforeAll
    static void beforeClass() {
        CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/yt1472");
        CODEC_FACTORY = JSONCodecFactorySupplier.RFC7951.getShared(CONTEXT);

        final var stack = SchemaInferenceStack.of(CONTEXT);
        stack.enterYangData(ERRORS_NAME);
        ERRORS_INFERENCE = stack.toInference();
    }

    @Test
    void testErrorsParsing() {
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, CODEC_FACTORY, ERRORS_INFERENCE);
        // https://www.rfc-editor.org/rfc/rfc8040#page-77
        jsonParser.parse(new JsonReader(new StringReader("""
            {
              "ietf-restconf:errors" : {
                "error" : [
                  {
                    "error-type" : "protocol",
                    "error-tag" : "lock-denied",
                    "error-message" : "Lock failed; lock already held"
                  }
                ]
              }
            }""")));
        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(NodeIdentifier.create(QName.create(RESTCONF_MODULE, "errors")))
            .withChild(Builders.unkeyedListBuilder()
                .withNodeIdentifier(ERROR_NID)
                .withChild(Builders.unkeyedListEntryBuilder()
                    .withNodeIdentifier(ERROR_NID)
                    .withChild(ImmutableNodes.leafNode(QName.create(RESTCONF_MODULE, "error-type"), "protocol"))
                    .withChild(ImmutableNodes.leafNode(QName.create(RESTCONF_MODULE, "error-tag"), "lock-denied"))
                    .withChild(ImmutableNodes.leafNode(QName.create(RESTCONF_MODULE, "error-message"),
                        "Lock failed; lock already held"))
                    .build())
                .build())
            .build(), result.getResult().data());
    }
}
