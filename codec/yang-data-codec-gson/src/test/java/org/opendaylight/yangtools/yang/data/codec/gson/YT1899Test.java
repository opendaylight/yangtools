/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Checks that {@link JsonParserStream} reuses its cached child lookups correctly. A list with more than one entry,
 * whose leaf sits inside a {@code choice}, is the only shape that looks up the same multi-step path twice.
 */
class YT1899Test {
    private static final QName TOP = QName.create("yt1899", "top");
    private static final QName FOO = QName.create(TOP, "foo");
    private static final QName NAME = QName.create(TOP, "name");
    private static final QName BAR = QName.create(TOP, "bar");
    private static final QName BAZ_LEAF = QName.create(TOP, "baz-leaf");

    private static final EffectiveModelContext MODEL_CONTEXT =
        YangParserTestUtils.parseYangResourceDirectory("/yt1899/yang");

    @Test
    void multiEntryListWithChoiceChild() throws Exception {
        final var inputJson = loadTextFile("/yt1899/json/multi-entry-list-with-choice.json");
        final var expected = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(fooEntry("one", "first"))
                .withChild(fooEntry("two", "second"))
                .build())
            .build();

        // The second list entry reuses the cached [bar, baz, baz-leaf] path, and the second parse reuses the cache for
        // everything, as both parses share the factory.
        final var shared = JSONCodecFactorySupplier.RFC7951.getShared(MODEL_CONTEXT);
        assertEquals(expected, parse(shared, inputJson));
        assertEquals(expected, parse(shared, inputJson));

        // A one-off factory starts with an empty cache.
        assertEquals(expected, parse(JSONCodecFactorySupplier.RFC7951.createLazy(MODEL_CONTEXT), inputJson));
    }

    private static MapEntryNode fooEntry(final String name, final String value) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, NAME, name))
            .withChild(ImmutableNodes.leafNode(NAME, name))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(ImmutableNodes.leafNode(BAZ_LEAF, value))
                .build())
            .build();
    }

    private static NormalizedNode parse(final JSONCodecFactory codecs, final String inputJson) throws IOException {
        final var holder = new NormalizationResultHolder();
        try (var writer = ImmutableNormalizedNodeStreamWriter.from(holder)) {
            try (var parser = JsonParserStream.create(writer, codecs)) {
                try (var reader = new JsonReader(new StringReader(inputJson))) {
                    parser.parse(reader);
                }
            }
        }
        return holder.getResult().data();
    }
}
