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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Checks how {@link JsonParserStream} finds the schema node of each JSON member: inside a {@code choice}, in another
 * module's namespace, below an {@code rpc}, when parsing starts at a {@code case}, and what happens to names which
 * name no data node, such as the name of a {@code choice} itself.
 */
class YT1899Test {
    private static final QName TOP = QName.create("yt1899", "top");
    private static final QName FOO = QName.create(TOP, "foo");
    private static final QName NAME = QName.create(TOP, "name");
    private static final QName BAR = QName.create(TOP, "bar");
    private static final QName BAZ = QName.create(TOP, "baz");
    private static final QName BAZ_LEAF = QName.create(TOP, "baz-leaf");
    private static final QName THUD = QName.create(TOP, "thud");
    private static final QName INPUT = QName.create(TOP, "input");
    private static final QName IN = QName.create(TOP, "in");
    private static final QName AUG_LEAF = QName.create("yt1899-aug", "aug-leaf");
    private static final QName AUG_CASE_LEAF = QName.create(AUG_LEAF, "aug-case-leaf");

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

        // The second list entry and the second parse look up children which were already looked up once
        final var shared = JSONCodecFactorySupplier.RFC7951.getShared(MODEL_CONTEXT);
        assertEquals(expected, parse(shared, inputJson));
        assertEquals(expected, parse(shared, inputJson));

        assertEquals(expected, parse(JSONCodecFactorySupplier.RFC7951.createLazy(MODEL_CONTEXT), inputJson));
    }

    @Test
    void choiceNameIsUnknownWhenStrict() {
        // "bar" is the name of the choice, which never appears in JSON
        final var ex = assertThrows(IllegalStateException.class, () -> parse(false, """
            {"yt1899:top":{"foo":[{"name":"one","bar":"x"}]}}"""));
        assertEquals("Schema node with name bar was not found under " + FOO + ".", ex.getMessage());
    }

    @Test
    void choiceNameIsSkippedWhenLenient() throws Exception {
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(fooEntry("one", "first"))
                .build())
            .build(), parse(true, """
                {"yt1899:top":{"foo":[{"name":"one","bar":"x","baz-leaf":"first"}]}}"""));
    }

    @Test
    void prefixedChoiceNameIsUnknown() {
        final var ex = assertThrows(IllegalStateException.class, () -> parse(true, """
            {"yt1899:top":{"foo":[{"name":"one","yt1899:bar":"x"}]}}"""));
        assertThat(ex.getMessage()).startsWith("Schema for node with name bar and namespace yt1899 does not exist at ");
    }

    @Test
    void unknownNameIsUnknownWhenStrict() {
        final var ex = assertThrows(IllegalStateException.class, () -> parse(false, """
            {"yt1899:top":{"foo":[{"name":"one","unknown":"x"}]}}"""));
        assertEquals("Schema node with name unknown was not found under " + FOO + ".", ex.getMessage());
    }

    @Test
    void unknownNameIsSkippedWhenLenient() throws Exception {
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(fooEntry("one", "first"))
                .build())
            .build(), parse(true, """
                {"yt1899:top":{"foo":[{"name":"one","unknown":"x","baz-leaf":"first"}]}}"""));
    }

    @Test
    void augmentedChildren() throws Exception {
        // One child is added to the list entry, the other to the choice, both from another module
        assertEquals(augmentedTop(), parse(false, """
            {"yt1899:top":{"foo":[{"name":"one","yt1899-aug:aug-leaf":"a","yt1899-aug:aug-case-leaf":"b"}]}}"""));
    }

    @Test
    void augmentedChildrenWithoutPrefix() throws Exception {
        // The names are not in the parent's namespace, so they are found by going through the parent's children
        assertEquals(augmentedTop(), parse(false, """
            {"yt1899:top":{"foo":[{"name":"one","aug-leaf":"a","aug-case-leaf":"b"}]}}"""));
    }

    @Test
    void rpcInput() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(THUD);
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(INPUT))
            .withChild(ImmutableNodes.leafNode(IN, "x"))
            .build(), parse(stack.toInference(), false, """
                {"yt1899:input":{"in":"x"}}"""));
    }

    @Test
    void startAtCase() throws Exception {
        assertEquals(ImmutableNodes.leafNode(BAZ_LEAF, "first"), parse(bazCase(), false, """
            {"yt1899:baz-leaf":"first"}"""));
    }

    @Test
    void startAtCaseRejectsOtherCase() {
        // Only the children of the case where parsing starts are known
        final var ex = assertThrows(IllegalStateException.class, () -> parse(bazCase(), false, """
            {"yt1899-aug:aug-case-leaf":"x"}"""));
        assertThat(ex.getMessage())
            .startsWith("Schema for node with name aug-case-leaf and namespace yt1899-aug does not exist at ");
    }

    private static EffectiveStatementInference bazCase() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(TOP);
        stack.enterSchemaTree(FOO);
        stack.enterChoice(BAR);
        stack.enterSchemaTree(BAZ);
        return stack.toInference();
    }

    private static NormalizedNode augmentedTop() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TOP))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, NAME, "one"))
                    .withChild(ImmutableNodes.leafNode(NAME, "one"))
                    .withChild(ImmutableNodes.leafNode(AUG_LEAF, "a"))
                    .withChild(ImmutableNodes.newChoiceBuilder()
                        .withNodeIdentifier(new NodeIdentifier(BAR))
                        .withChild(ImmutableNodes.leafNode(AUG_CASE_LEAF, "b"))
                        .build())
                    .build())
                .build())
            .build();
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

    private static NormalizedNode parse(final boolean lenient, final String inputJson) throws IOException {
        return parse(SchemaInferenceStack.of(MODEL_CONTEXT).toInference(), lenient, inputJson);
    }

    private static NormalizedNode parse(final EffectiveStatementInference inference, final boolean lenient,
            final String inputJson) throws IOException {
        final var codecs = JSONCodecFactorySupplier.RFC7951.getShared(MODEL_CONTEXT);
        final var holder = new NormalizationResultHolder();
        try (var writer = ImmutableNormalizedNodeStreamWriter.from(holder)) {
            try (var parser = lenient ? JsonParserStream.createLenient(writer, codecs, inference)
                : JsonParserStream.create(writer, codecs, inference)) {
                try (var reader = new JsonReader(new StringReader(inputJson))) {
                    parser.parse(reader);
                }
            }
        }
        return holder.getResult().data();
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
