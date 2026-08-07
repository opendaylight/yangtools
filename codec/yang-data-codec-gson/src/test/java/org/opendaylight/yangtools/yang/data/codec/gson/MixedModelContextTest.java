/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.assertj.core.api.Assertions.assertThat;
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
 *
 * <p>The second group of tests measures how far apart the two contexts can drift before anything complains. The
 * answer is: very far. The factory's context is never asked about structure -- it supplies a module name to namespace
 * mapping and, for identityref and instance-identifier only, the values. Two contexts agreeing on nothing but a
 * module's name and namespace parse happily; the tree comes out entirely from the inference's context. What does
 * fail, loudly, is a factory context that maps that module name to a <i>different</i> namespace.
 *
 * <p>One thing deliberately not covered here: a factory from
 * {@link JSONCodecFactorySupplier#getPrecomputed(EffectiveModelContext)} rejects any foreign schema node outright,
 * even a plain string leaf, because its codec cache is keyed on object identity and has no entry to find. That is a
 * property of the cache rather than of the parser.
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
    // Same module name, namespace and revision as CTX_2023, but sharing none of its contents.
    private static final EffectiveModelContext CTX_DIVERGENT =
        YangParserTestUtils.parseYangResourceDirectory("/mixed-ctx/divergent");
    // Same module name as CTX_2023, different namespace.
    private static final EffectiveModelContext CTX_OTHER_NS =
        YangParserTestUtils.parseYangResourceDirectory("/mixed-ctx/other-ns");
    // Shares nothing with CTX_2023, not even the module name.
    private static final EffectiveModelContext CTX_UNRELATED =
        YangParserTestUtils.parseYangResourceDirectory("/mixed-ctx/unrelated");

    private static final NodeIdentifier TOP_2023 = new NodeIdentifier(QName.create(NAMESPACE, REV_2023, "top"));
    private static final NodeIdentifier NAME_2023 = new NodeIdentifier(QName.create(NAMESPACE, REV_2023, "name"));
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
     * How little the two contexts have to agree on. The factory's context here has the same module name and namespace
     * as the inference's, and nothing else: no {@code top}, no {@code name}, a different container and a different
     * identity. The parse still succeeds, because the only thing taken from the factory's context is the namespace
     * its module name maps to.
     */
    @Test
    void divergentModelsStillParse() {
        final var top = parse(CTX_DIVERGENT, """
            {
              "mixed-ctx-foo:top" : {
                "name" : "hello"
              }
            }""");

        // the whole tree is built from the inference's context
        assertEquals(TOP_2023, top.name());
        assertEquals("hello", top.getChildByArg(NAME_2023).body());
    }

    /**
     * A prefix the factory's context cannot resolve is ignored rather than rejected. {@code findModuleStatements}
     * comes back empty, the namespace stays null, and resolution falls back to searching the inference's context for
     * a child of that local name.
     */
    @Test
    void unknownPrefixFallsBackToTheInferenceContext() {
        final var top = parse(CTX_UNRELATED, """
            {
              "mixed-ctx-foo:top" : {
                "name" : "hello"
              }
            }""");

        assertEquals(TOP_2023, top.name());
        assertEquals("hello", top.getChildByArg(NAME_2023).body());
    }

    /**
     * Why the fallback above happens at all -- it is not specific to mixed contexts. Even with a single context on
     * both sides, a prefix naming no known module is silently dropped and the local name resolved on its own.
     */
    @Test
    void bogusPrefixIsIgnored() {
        final var top = parse(CTX_2023, """
            {
              "totally-bogus:top" : {
                "name" : "hello"
              }
            }""");

        assertEquals(TOP_2023, top.name());
        assertEquals("hello", top.getChildByArg(NAME_2023).body());
    }

    /**
     * Where the silence stops. When the factory's context maps the module name to a <i>different</i> namespace, that
     * namespace is what the child lookup searches for, and the inference's context has nothing under it.
     *
     * <p>Note this defeats lenient mode too: the lenient skip only fires when no namespace could be resolved at all,
     * and here one was -- just the wrong one.
     */
    @Test
    void conflictingNamespaceFailsLoudly() {
        final var json = """
            {
              "mixed-ctx-foo:top" : {
                "name" : "hello"
              }
            }""";

        final var ex = assertThrows(IllegalStateException.class, () -> parse(CTX_OTHER_NS, json));
        assertThat(ex.getMessage()).startsWith(
            "Schema for node with name top and namespace urn:opendaylight:yangtools:mixed-ctx-other does not exist at");

        final var holder = new NormalizationResultHolder();
        final var writer = ImmutableNormalizedNodeStreamWriter.from(holder);
        final var lenient = JsonParserStream.createLenient(writer,
            JSONCodecFactorySupplier.RFC7951.getShared(CTX_OTHER_NS), Inference.of(CTX_2023));
        final var lenientEx = assertThrows(IllegalStateException.class,
            () -> lenient.parse(new JsonReader(new StringReader(json))));
        assertEquals(ex.getMessage(), lenientEx.getMessage());
    }

    /**
     * Parse {@code json} with the deliberately mismatched pairing: tree walk driven by the 2023 context, codecs taken
     * from the 2024 context.
     */
    private static ContainerNode parseMixed(final String json) {
        return parse(CTX_2024, json);
    }

    /**
     * Parse {@code json} with the tree walk driven by {@link #CTX_2023} and codecs taken from {@code factoryContext}.
     */
    private static ContainerNode parse(final EffectiveModelContext factoryContext, final String json) {
        final var holder = new NormalizationResultHolder();
        final var writer = ImmutableNormalizedNodeStreamWriter.from(holder);
        JsonParserStream.create(writer, JSONCodecFactorySupplier.RFC7951.getShared(factoryContext),
                Inference.of(CTX_2023))
            .parse(new JsonReader(new StringReader(json)));
        return assertInstanceOf(ContainerNode.class, holder.getResult().data());
    }
}
