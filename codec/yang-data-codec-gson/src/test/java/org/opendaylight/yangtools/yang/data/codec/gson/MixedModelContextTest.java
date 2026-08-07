/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * What happens when a {@link JsonParserStream} is handed two different {@link EffectiveModelContext}s: one carried by
 * the {@link Inference}, another carried by the {@link JSONCodecFactory}.
 *
 * <p>The two contexts have separate jobs. The inference's context drives the schema tree walk, so every <b>node</b>
 * name in the resulting tree comes from it. The factory's context is used for only two things: mapping a JSON prefix
 * to a namespace, and looking up the codec that turns a JSON string into a <b>value</b>. When the two contexts hold
 * the same module at different revisions, those two jobs disagree, and the disagreement is invisible while parsing.
 *
 * <p>It is invisible because of an asymmetry in how yangtools identifies things. An {@link XMLNamespace} is shared by
 * every revision of a module, so looking up a child element by namespace succeeds no matter which context answers.
 * A {@link QName}, though, carries namespace <i>and</i> revision, so a value decoded into a QName silently picks up
 * the revision of whichever context decoded it.
 *
 * <p>These tests use {@code mixed-ctx-foo}, one module present at revisions 2023-01-01 and 2024-01-01 under a single
 * namespace, and deliberately pair an inference from the 2023 context with codecs from the 2024 context.
 */
class MixedModelContextTest {
    private static final XMLNamespace NAMESPACE = XMLNamespace.of("urn:opendaylight:yangtools:mixed-ctx-foo");
    private static final Revision REV_2023 = Revision.of("2023-01-01");
    private static final Revision REV_2024 = Revision.of("2024-01-01");

    // The context the Inference comes from -- it decides the node names in the parsed tree.
    private static final EffectiveModelContext CTX_2023 =
        YangParserTestUtils.parseYangResourceDirectory("/mixed-ctx/v2023");
    // The context the JSONCodecFactory comes from -- it decides the values.
    private static final EffectiveModelContext CTX_2024 =
        YangParserTestUtils.parseYangResourceDirectory("/mixed-ctx/v2024");

    private static final NodeIdentifier TOP_2023 = new NodeIdentifier(QName.create(NAMESPACE, REV_2023, "top"));
    private static final NodeIdentifier ID_REF_2023 = new NodeIdentifier(QName.create(NAMESPACE, REV_2023, "id-ref"));
    private static final NodeIdentifier IID_2023 = new NodeIdentifier(QName.create(NAMESPACE, REV_2023, "iid"));

    /**
     * A prefixed identityref value. The prefix {@code mixed-ctx-foo:} is resolved against the factory's context, so
     * the decoded QName carries the factory's revision -- while the leaf holding it carries the inference's revision.
     */
    @Test
    void prefixedIdentityrefPicksUpTheFactorysRevision() {
        final var top = parseMixed("""
            {
              "mixed-ctx-foo:top" : {
                "id-ref" : "mixed-ctx-foo:my-identity"
              }
            }""");

        // the NODE identities come from the inference's context ...
        assertEquals(TOP_2023, top.name());
        final var idRef = top.getChildByArg(ID_REF_2023);

        // ... while the identityref VALUE comes from the factory's context
        final var value = assertInstanceOf(QName.class, idRef.body());
        assertEquals(NAMESPACE, value.getNamespace());
        assertEquals(REV_2024, value.getRevision().orElseThrow());
    }

    /**
     * The same split for an instance-identifier value: every step of the parsed path is resolved against the
     * factory's {@code DataSchemaContextTree}, so the path carries the factory's revision.
     */
    @Test
    void instanceIdentifierPicksUpTheFactorysRevision() {
        final var top = parseMixed("""
            {
              "mixed-ctx-foo:top" : {
                "iid" : "/mixed-ctx-foo:top"
              }
            }""");

        assertEquals(TOP_2023, top.name());

        final var value = assertInstanceOf(YangInstanceIdentifier.class, top.getChildByArg(IID_2023).body());
        final var step = assertInstanceOf(NodeIdentifier.class, value.getLastPathArgument());
        assertEquals(NAMESPACE, step.getNodeType().getNamespace());
        assertEquals(REV_2024, step.getNodeType().getRevision().orElseThrow());
    }

    /**
     * The negative control, showing the silent window is specifically the prefixed one. An unprefixed value is
     * resolved against the module owning the leaf -- which belongs to the inference's context, revision 2023-01-01.
     * The factory's context does not have that revision, so the lookup fails instead of quietly returning the wrong
     * thing.
     */
    @Test
    void unprefixedIdentityrefFailsLoudly() {
        final var ex = assertThrows(IllegalStateException.class, () -> parseMixed("""
            {
              "mixed-ctx-foo:top" : {
                "id-ref" : "my-identity"
              }
            }"""));
        assertEquals("Parsed QName (urn:opendaylight:yangtools:mixed-ctx-foo?revision=2023-01-01)my-identity refers "
            + "to a non-existent module", ex.getMessage());
    }

    /**
     * Parse {@code json} with the deliberately mismatched pairing: tree walk driven by the 2023 context, codecs taken
     * from the 2024 context.
     */
    private static ContainerNode parseMixed(final String json) {
        final var holder = new NormalizationResultHolder();
        final var writer = ImmutableNormalizedNodeStreamWriter.from(holder);
        JsonParserStream.create(writer, JSONCodecFactorySupplier.RFC7951.getShared(CTX_2024), Inference.of(CTX_2023))
            .parse(new JsonReader(new StringReader(json)));
        return assertInstanceOf(ContainerNode.class, holder.getResult().data());
    }
}
