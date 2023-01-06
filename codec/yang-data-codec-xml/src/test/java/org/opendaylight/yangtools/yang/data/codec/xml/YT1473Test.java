/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.io.StringReader;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@ExtendWith(MockitoExtension.class)
public class YT1473Test {

    private static final String FOO_NS = "foons"; // namespace for prefix 'foo'
    private static final QName FOO_FOO = QName.create(FOO_NS, "foo"); // list with key 'str'
    private static final QName FOO_BAR = QName.create(FOO_NS, "bar"); // list with key 'qname'
    private static final QName FOO_BAZ = QName.create(FOO_NS, "baz"); // list with key 'id'
    private static final QName FOO_ONE = QName.create(FOO_NS, "one"); // identity
    private static final QName FOO_STR = QName.create(FOO_NS, "str"); // key of type 'string'
    private static final QName FOO_QNAME = QName.create(FOO_NS, "qname"); // key of type 'one' based
    private static final QName FOO_ID = QName.create(FOO_NS, "id"); // key of type 'instance-identifier'

    private static final String BAR_NS = "barns"; // namespace for prefix 'bar'
    private static final QName BAR_TWO = QName.create(BAR_NS, "two"); // identity inheriting 'foo:one'
    private static final QName BAR_STR = QName.create(BAR_NS, "str"); // leaf-list of type 'string'
    private static final QName BAR_FOO = QName.create(BAR_NS, "foo"); // leaf-list of type 'foo:one' based
    private static final QName BAR_BAR = QName.create(BAR_NS, "bar"); // leaf-list of type 'instance-identifier'
    private static final QName BAR_BAZ = QName.create(BAR_NS, "baz"); // leaf of type 'instance-identifier'

    private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private static final String XMLNS = "xmlns:foo=\"foons\" xmlns:bar=\"barns\"";

    private static EffectiveModelContext MODEL_CONTEXT;
    private static XmlCodec<YangInstanceIdentifier> CODEC;

    @Mock
    private XMLStreamWriter writer;
    @Captor
    private ArgumentCaptor<String> captor;

    @BeforeAll
    public static void beforeAll() {
        MODEL_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/yt1473");
        final var baz = MODEL_CONTEXT.getDataChildByName(FOO_BAZ);
        assertTrue(baz instanceof ListSchemaNode);
        final var id = ((ListSchemaNode) baz).getDataChildByName(FOO_ID);
        assertTrue(id instanceof LeafSchemaNode);
        final var type = ((LeafSchemaNode) id).getType();
        assertTrue(type instanceof InstanceIdentifierTypeDefinition);
        CODEC = (XmlStringInstanceIdentifierCodec) XmlCodecFactory.create(MODEL_CONTEXT)
                .instanceIdentifierCodec((InstanceIdentifierTypeDefinition) type);
    }

    @AfterAll
    public static void afterAll() {
        MODEL_CONTEXT = null;
        CODEC = null;
    }

    @ParameterizedTest(name = "Serialize key value: {0}")
    @MethodSource("testArgs")
    void serialize(final String output, final YangInstanceIdentifier input) throws Exception {
        assertEquals(output, write(input));
    }

    @ParameterizedTest(name = "Parse key value: {0}")
    @MethodSource("testArgs")
    void parse(final String input, final YangInstanceIdentifier output) throws Exception {

        // case 1: list with key of type 'instance-identifier'
        final String xml1 = XML_HEAD + "<baz xmlns=\"foons\"><id " + XMLNS + ">" + input + "</id></baz>";
        final NormalizedNode normalizedNode1 = xmlToNormalizedNode(xml1, FOO_BAZ);
        assertEquals(new NodeIdentifier(FOO_BAZ), normalizedNode1.getIdentifier());
        assertTrue(normalizedNode1 instanceof MapNode);
        final MapNode mapNode = (MapNode) normalizedNode1;
        assertEquals(1, mapNode.size());
        assertNotNull(mapNode.childByArg(NodeIdentifierWithPredicates.of(FOO_BAZ, FOO_ID, output)));

        // case 2: leaf of type 'instance-identifier'
        final String xml2 = XML_HEAD + "<bar:baz " + XMLNS + ">" + input + "</bar:baz>";
        final NormalizedNode normalizedNode2 = xmlToNormalizedNode(xml2, BAR_BAZ);
        assertEquals(new NodeIdentifier(BAR_BAZ), normalizedNode2.getIdentifier());
        assertEquals(output, normalizedNode2.body());
    }

    private static Stream<Arguments> testArgs() {
        return Stream.of(
                // strings
                Arguments.of("/foo:foo[foo:str='str\"']", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str\"")),
                Arguments.of("/bar:str[.='str\"']", buildYangInstanceIdentifier(BAR_STR,"str\"")),
                Arguments.of("/foo:foo[foo:str=\"str'\\\"\"]", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str'\"")),
                Arguments.of("/bar:str[.=\"str'\\\"\"]", buildYangInstanceIdentifier(BAR_STR,"str'\"")),
                // identity-ref
                Arguments.of("/foo:bar[foo:qname='foo:one']",
                        buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE)),
                Arguments.of("/bar:foo[.='foo:one']", buildYangInstanceIdentifier(BAR_FOO, FOO_ONE)),
                Arguments.of("/foo:bar[foo:qname='bar:two']",
                        buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)),
                Arguments.of("/bar:foo[.='bar:two']", buildYangInstanceIdentifier(BAR_FOO, BAR_TWO))
        );
    }

    @Test
    @Disabled("YT-1473: Instance-identifier values need to be recognized and properly encoded and escaped")
    public void testSerializeInstanceIdentifierRef() throws Exception {
        assertEquals("/foo:baz[id=\"/foo:bar[qname='bar:two']\"]", write(
                buildYangInstanceIdentifier(FOO_BAZ, FOO_ID, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)))
        );
    }

    @Test
    @Disabled("YT-1473: Instance-identifier values need to be recognized and properly encoded and escaped")
    public void testSerializeInstanceIdentifierValue() throws Exception {
        assertEquals("/bar:bar[.=\"/foo:bar/bar[qname='bar:two'\"]']",
                write(buildYangInstanceIdentifier(BAR_BAR, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO))));
    }

    private static YangInstanceIdentifier buildYangInstanceIdentifier(final QName node, final QName key,
            final Object value) {
        return YangInstanceIdentifier.create(
                new NodeIdentifier(node), NodeIdentifierWithPredicates.of(node, key, value));
    }

    private static YangInstanceIdentifier buildYangInstanceIdentifier(final QName node, final Object value) {
        return YangInstanceIdentifier.create(new NodeWithValue<>(node, value));
    }

    private String write(final YangInstanceIdentifier yangInstanceIdentifier) throws Exception {
        CODEC.writeValue(writer, yangInstanceIdentifier);
        verify(writer).writeCharacters(captor.capture());
        return captor.getValue();
    }

    private static NormalizedNode xmlToNormalizedNode(final String xml, final QName qname) throws Exception {
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(new StringReader(xml));
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
                Inference.ofDataTreePath(MODEL_CONTEXT, qname));
        xmlParser.parse(reader);
        return result.getResult();
    }

}
