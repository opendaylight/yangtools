/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1899Test {
    private static final QName TOP_LIST = QName.create("yt1899", "top-list");
    private static final QName CHOICE_NODE = QName.create("yt1899", "choice-node");
    private static final QName CASE_A_LEAF = QName.create("yt1899", "case-a-leaf");

    private static final EffectiveModelContext MODEL_CONTEXT =
        YangParserTestUtils.parseYangResourceDirectory("/yt1899");

    /**
     * {@code case-a-leaf} resolves to a {@code [choice-node, case-a, case-a-leaf]} path, which
     * {@link JsonParserStream} memoizes per parent node and hands to {@code addChild()}, which consumes it. The list
     * is unkeyed and has no other child, so this is the only path resolved more than once and a failure here points
     * at the lookup cache rather than at the model.
     */
    @Test
    void memoizedChoicePathIsReusable() throws IOException, URISyntaxException {
        final var inputJson = loadTextFile("/yt1899/json/repeated-choice-child.json");

        // Entries 2 and 3 are served from the path memoized while parsing entry 1
        final var first = assertDoesNotThrow(() -> parse(inputJson),
            "Repeated lookups must each be given an independent copy of the memoized path");
        assertEquals(expectedTopList(), first);

        // A second parser instance starts out with the path already memoized
        final var second = assertDoesNotThrow(() -> parse(inputJson),
            "A parse served entirely from memoized paths must yield the same result");
        assertEquals(first, second);
    }

    private static UnkeyedListNode expectedTopList() {
        return ImmutableNodes.newUnkeyedListBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP_LIST))
            .withChild(topListEntry("first"))
            .withChild(topListEntry("second"))
            .withChild(topListEntry("third"))
            .build();
    }

    private static UnkeyedListEntryNode topListEntry(final String value) {
        return ImmutableNodes.newUnkeyedListEntryBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP_LIST))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(CHOICE_NODE))
                .withChild(ImmutableNodes.leafNode(CASE_A_LEAF, value))
                .build())
            .build();
    }

    private static NormalizedNode parse(final String json) {
        final var holder = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(holder);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.RFC7951.getShared(MODEL_CONTEXT));
        jsonParser.parse(new JsonReader(new StringReader(json)));
        return holder.getResult().data();
    }
}
