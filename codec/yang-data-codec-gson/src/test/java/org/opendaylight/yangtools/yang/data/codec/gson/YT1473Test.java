/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@ExtendWith(MockitoExtension.class)
class YT1473Test {

    private static final String FOO_NS = "foons"; // namespace for prefix 'foo'
    private static final QName FOO_FOO = QName.create(FOO_NS, "foo"); // list with key 'str'
    private static final QName FOO_BAR = QName.create(FOO_NS, "bar"); // list with key 'qname'
    private static final QName FOO_BAZ = QName.create(FOO_NS, "baz"); // list with key 'id'
    private static final QName FOO_BEE = QName.create(FOO_NS, "bee"); // list with key 'bts'
    private static final QName FOO_ONE = QName.create(FOO_NS, "one"); // identity
    private static final QName FOO_STR = QName.create(FOO_NS, "str"); // key of type 'string'
    private static final QName FOO_QNAME = QName.create(FOO_NS, "qname"); // key of type 'one' based
    private static final QName FOO_ID = QName.create(FOO_NS, "id"); // key of type 'instance-identifier'
    private static final QName FOO_BTS = QName.create(FOO_NS, "bts"); // key of type 'bts' (bits)

    private static final String BAR_NS = "barns"; // namespace for prefix 'bar'
    private static final QName BAR_TWO = QName.create(BAR_NS, "two"); // identity inheriting 'foo:one'
    private static final QName BAR_STR = QName.create(BAR_NS, "str"); // leaf-list of type 'string'
    private static final QName BAR_FOO = QName.create(BAR_NS, "foo"); // leaf-list of type 'foo:one' based
    private static final QName BAR_BAR = QName.create(BAR_NS, "bar"); // leaf-list of type 'instance-identifier'
    private static final QName BAR_BEE = QName.create(BAR_NS, "bee"); // leaf-list of type 'foo:bts' (bits)
    private static final QName BAR_BAZ = QName.create(BAR_NS, "baz"); // leaf of type 'instance-identifier'


    private static final Gson GSON = new GsonBuilder().create();
    private static JSONCodecFactory CODEC_FACTORY;
    private static JSONCodec<YangInstanceIdentifier> CODEC;

    @Mock
    private JsonWriter writer;
    @Captor
    private ArgumentCaptor<String> captor;

    @BeforeAll
    static void beforeAll() {
        final var modelContext = YangParserTestUtils.parseYangResourceDirectory("/yt1473");
        final var baz = modelContext.getDataChildByName(FOO_BAZ);
        assertTrue(baz instanceof ListSchemaNode);
        final var id = ((ListSchemaNode) baz).getDataChildByName(FOO_ID);
        assertTrue(id instanceof LeafSchemaNode);
        final var type = ((LeafSchemaNode) id).getType();
        assertTrue(type instanceof InstanceIdentifierTypeDefinition);
        CODEC_FACTORY = JSONCodecFactorySupplier.RFC7951.getShared(modelContext);
        CODEC = CODEC_FACTORY.instanceIdentifierCodec((InstanceIdentifierTypeDefinition) type);
    }

    @AfterAll
    static void afterAll() {
        CODEC_FACTORY = null;
        CODEC = null;
    }

    @ParameterizedTest(name = "Serialize key value: {0}")
    @MethodSource("testArgs")
    void serialize(final String output, final YangInstanceIdentifier input) throws Exception {
        doReturn(writer).when(writer).value(anyString());
        CODEC.writeValue(writer, input);
        verify(writer).value(captor.capture());
        assertEquals(output, captor.getValue());
    }

    @ParameterizedTest(name = "Parse key value: {0}")
    @MethodSource("testArgs")
    void parse(final String input, final YangInstanceIdentifier output) throws Exception {

        // using GSON for ease of escaping quotes within JSON value

        // case 1: list with key of type 'instance-identifier' -> { "foo:baz" : [ { "id" : "<input>" } ] }
        final String json1 = GSON.toJson(Map.of("foo:baz", List.of(Map.of("id", input))));
        final NormalizedNode normalizedNode1 = jsonToNormalizedNode(json1);
        assertEquals(new NodeIdentifier(FOO_BAZ), normalizedNode1.getIdentifier());
        assertTrue(normalizedNode1 instanceof MapNode);
        final MapNode mapNode = (MapNode) normalizedNode1;
        assertEquals(1, mapNode.size());
        final MapEntryNode child = mapNode.body().iterator().next();
        assertEquals(NodeIdentifierWithPredicates.of(FOO_BAZ, FOO_ID, output), child.getIdentifier());

        // case 2: leaf of type 'instance-identifier' -> { "bar:baz" : "<input>" }
        final String json2 = GSON.toJson(Map.of("bar:baz", input));
        final NormalizedNode normalizedNode2 = jsonToNormalizedNode(json2);
        assertEquals(new NodeIdentifier(BAR_BAZ), normalizedNode2.getIdentifier());
        assertEquals(output, normalizedNode2.body());
    }

    private static Stream<Arguments> testArgs() {
        return Stream.of(
                // strings
                Arguments.of("/foo:foo[str='str\"']", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str\"")),
                Arguments.of("/bar:str[.='str\"']", buildYangInstanceIdentifier(BAR_STR, "str\"")),
                Arguments.of("/foo:foo[str=\"str'\\\"\"]", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str'\"")),
                Arguments.of("/bar:str[.=\"str'\\\"\"]", buildYangInstanceIdentifier(BAR_STR, "str'\"")),
                // bits
                Arguments.of("/foo:bee[bts='one two']",
                        buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of("one", "two"))),
                Arguments.of("/bar:bee[.='two three']",
                        buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of("two", "three"))),
                // identity-ref
                Arguments.of("/foo:bar[qname='one']", buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE)),
                Arguments.of("/bar:foo[.='foo:one']", buildYangInstanceIdentifier(BAR_FOO, FOO_ONE)),
                Arguments.of("/foo:bar[qname='bar:two']", buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)),
                Arguments.of("/bar:foo[.='two']", buildYangInstanceIdentifier(BAR_FOO, BAR_TWO)),
                // instance-identifier
                Arguments.of("/foo:baz[id=\"/foo:bar[qname='bar:two']\"]", buildYangInstanceIdentifier(FOO_BAZ, FOO_ID,
                        buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO))),
                Arguments.of("/bar:bar[.=\"/foo:bar[qname='one']\"]", buildYangInstanceIdentifier(BAR_BAR,
                        buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE)))
        );
    }

    private static YangInstanceIdentifier buildYangInstanceIdentifier(final QName node, final QName key,
            final Object value) {
        return YangInstanceIdentifier.create(
                new NodeIdentifier(node), NodeIdentifierWithPredicates.of(node, key, value));
    }

    private static YangInstanceIdentifier buildYangInstanceIdentifier(final QName node, final Object value) {
        return YangInstanceIdentifier.create(new NodeWithValue<>(node, value));
    }

    private static NormalizedNode jsonToNormalizedNode(final String json) throws Exception {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, CODEC_FACTORY);
        jsonParser.parse(new JsonReader(new StringReader(json)));
        return result.getResult();
    }
}
