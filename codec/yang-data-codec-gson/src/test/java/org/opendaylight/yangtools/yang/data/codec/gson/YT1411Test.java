/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;

class YT1411Test extends AbstractComplexJsonTest {
    @Test
    void testChoiceCaseChoiceIdentifier() {
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.RFC7951.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader("""
            {
              "complexjson:cont1": {
                "lst11": [
                  {
                    "key111": "foo",
                    "lf111": "bar",
                    "lf112": "/complexjson:cont1/case11-choice-case-container"
                  }
                ]
              }
            }""")));
        final var cont1 = assertInstanceOf(ContainerNode.class, result.getResult().data());

        final var lst11 = QName.create(CONT_1, "lst11");
        final var lf112 = assertInstanceOf(LeafNode.class, NormalizedNodes.findNode(cont1,
            new NodeIdentifier(lst11),
            NodeIdentifierWithPredicates.of(lst11,
                Map.of(QName.create(CONT_1, "key111"), "foo", QName.create(CONT_1, "lf111"), "bar")),
            new NodeIdentifier(QName.create(CONT_1, "lf112")))
            .orElseThrow());

        final var augmentChoice1 = QName.create(CONT_1, "augment-choice1");
        assertEquals(YangInstanceIdentifier.of(
            CONT_1, augmentChoice1, QName.create(CONT_1, "augment-choice2"),
            QName.create(CONT_1, "case11-choice-case-container")),
            lf112.body());
    }
}
