/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * A {@link JsonParserStream} must stay within the model context of the {@link JSONCodecFactory} it was given. Parsing
 * against a different one would resolve {@code identityref} and {@code instance-identifier} values against the wrong
 * schema, and it is also what keeps every {@link SchemaLookupCache} entry within a single model context.
 */
class JsonParserStreamModelContextTest {
    private static final QName TOP = QName.create("yt1899", "top");

    // The same models parsed twice: the two contexts hold equal content, but they are distinct objects, which is
    // exactly the mismatch a caller can make by accident -- for example by keeping an inference from before a model
    // update and using it with a factory for the context that replaced it.
    private static final EffectiveModelContext MODEL_CONTEXT =
        YangParserTestUtils.parseYangResourceDirectory("/yt1899/yang");
    private static final EffectiveModelContext OTHER_MODEL_CONTEXT =
        YangParserTestUtils.parseYangResourceDirectory("/yt1899/yang");

    private static final JSONCodecFactory CODECS = JSONCodecFactorySupplier.RFC7951.getShared(MODEL_CONTEXT);

    @Test
    void createRejectsForeignInference() {
        final var foreign = Inference.ofDataTreePath(OTHER_MODEL_CONTEXT, TOP);
        assertMismatch(assertThrows(IllegalArgumentException.class,
            () -> JsonParserStream.create(writer(), CODECS, foreign)));
    }

    @Test
    void createLenientRejectsForeignInference() {
        final var foreign = Inference.ofDataTreePath(OTHER_MODEL_CONTEXT, TOP);
        assertMismatch(assertThrows(IllegalArgumentException.class,
            () -> JsonParserStream.createLenient(writer(), CODECS, foreign)));
    }

    @Test
    void createRejectsForeignRootInference() {
        // An empty inference points at the root of its model context, so it carries no statements at all -- only the
        // model context, which is enough to be wrong.
        final var foreign = Inference.of(OTHER_MODEL_CONTEXT);
        assertMismatch(assertThrows(IllegalArgumentException.class,
            () -> JsonParserStream.create(writer(), CODECS, foreign)));
    }

    @Test
    void createAcceptsOwnInference() throws Exception {
        try (var parser = JsonParserStream.create(writer(), CODECS, Inference.ofDataTreePath(MODEL_CONTEXT, TOP))) {
            // Nothing to do, constructing the parser is the whole test
        }
    }

    private static void assertMismatch(final IllegalArgumentException ex) {
        assertTrue(ex.getMessage().startsWith("Mismatched inference, expecting model context "), ex.getMessage());
    }

    private static NormalizedNodeStreamWriter writer() {
        return ImmutableNormalizedNodeStreamWriter.from(new NormalizationResultHolder());
    }
}