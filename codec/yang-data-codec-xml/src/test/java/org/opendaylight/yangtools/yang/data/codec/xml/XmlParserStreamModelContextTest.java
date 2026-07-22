/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.toIdentityString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * An {@link XmlParserStream} must stay within the model context of the {@link XmlCodecFactory} it was given. Parsing
 * against a different one would resolve {@code identityref} and {@code instance-identifier} values against the wrong
 * schema.
 */
class XmlParserStreamModelContextTest {
    private static final QName PARENT_CONTAINER = QName.create("foo-namespace", "parent-container");

    // The same model parsed twice: the two contexts hold equal content, but they are distinct objects, which is
    // exactly the mismatch a caller can make by accident -- for example by keeping an inference from before a model
    // update and using it with a factory for the context that replaced it.
    private static final EffectiveModelContext MODEL_CONTEXT = YangParserTestUtils.parseYangResource("/foo.yang");
    private static final EffectiveModelContext OTHER_MODEL_CONTEXT =
        YangParserTestUtils.parseYangResource("/foo.yang");

    private static final XmlCodecFactory CODECS = XmlCodecFactory.create(MODEL_CONTEXT);

    @Test
    void createRejectsForeignInference() {
        final var foreign = Inference.ofDataTreePath(OTHER_MODEL_CONTEXT, PARENT_CONTAINER);
        assertMismatch(assertThrows(IllegalArgumentException.class,
            () -> XmlParserStream.create(writer(), CODECS, foreign)));
    }

    @Test
    void createRejectsForeignInferenceInLenientMode() {
        final var foreign = Inference.ofDataTreePath(OTHER_MODEL_CONTEXT, PARENT_CONTAINER);
        assertMismatch(assertThrows(IllegalArgumentException.class,
            () -> XmlParserStream.create(writer(), CODECS, foreign, false)));
    }

    @Test
    void createRejectsForeignRootInference() {
        // An empty inference points at the root of its model context, so it carries no statements at all -- only the
        // model context, which is enough to be wrong.
        final var foreign = Inference.of(OTHER_MODEL_CONTEXT);
        assertMismatch(assertThrows(IllegalArgumentException.class,
            () -> XmlParserStream.create(writer(), CODECS, foreign)));
    }

    @Test
    void createRejectsForeignInferenceForAMountPoint() {
        // The mount point variant builds its own codecs from mountCtx, so the mismatch is between the inference and
        // the mount point rather than between the inference and a caller-supplied factory.
        final var mountCtx = MountPointContext.of(MODEL_CONTEXT);
        final var foreign = Inference.ofDataTreePath(OTHER_MODEL_CONTEXT, PARENT_CONTAINER);
        assertMismatch(assertThrows(IllegalArgumentException.class,
            () -> XmlParserStream.create(writer(), mountCtx, foreign)));
    }

    @Test
    void createAcceptsOwnInference() throws Exception {
        final var inference = Inference.ofDataTreePath(MODEL_CONTEXT, PARENT_CONTAINER);
        try (var parser = XmlParserStream.create(writer(), CODECS, inference)) {
            // Constructing the parser is the whole test: it is rejecting the inference that would throw.
            assertNotNull(parser);
        }
    }

    private static void assertMismatch(final IllegalArgumentException ex) {
        assertEquals("Mismatched inference: expecting model context " + toIdentityString(MODEL_CONTEXT) + ", got "
            + toIdentityString(OTHER_MODEL_CONTEXT), ex.getMessage());
    }

    private static NormalizedNodeStreamWriter writer() {
        return ImmutableNormalizedNodeStreamWriter.from(new NormalizationResultHolder());
    }
}
