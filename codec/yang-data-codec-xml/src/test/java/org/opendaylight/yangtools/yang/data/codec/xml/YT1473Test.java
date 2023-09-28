/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
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

    private static XmlCodec<YangInstanceIdentifier> CODEC;

    @BeforeAll
    static void beforeAll() {
        final var modelContext = YangParserTestUtils.parseYangResourceDirectory("/yt1473");
        final var baz = assertInstanceOf(ListSchemaNode.class, modelContext.getDataChildByName(FOO_BAZ));
        final var id = assertInstanceOf(LeafSchemaNode.class, baz.getDataChildByName(FOO_ID));
        final var type = assertInstanceOf(InstanceIdentifierTypeDefinition.class, id.getType());
        CODEC = XmlCodecFactory.create(modelContext).instanceIdentifierCodec(type);
    }

    @AfterAll
    static void afterAll() {
        CODEC = null;
    }

    @Test
    void testSerializeSimple() throws Exception {
        // No escaping needed, use single quotes
        assertBar("/bar:str[.='str\"']", buildYangInstanceIdentifier(BAR_STR, "str\""));
        assertBar("/bar:str[.='str\\']", buildYangInstanceIdentifier(BAR_STR, "str\\"));
        assertBar("/bar:str[.='str\r']", buildYangInstanceIdentifier(BAR_STR, "str\r"));
        assertBar("/bar:str[.='str\n']", buildYangInstanceIdentifier(BAR_STR, "str\n"));
        assertBar("/bar:str[.='str\t']", buildYangInstanceIdentifier(BAR_STR, "str\t"));

        assertFoo("/foo:foo[foo:str='str\"\\']", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str\"\\"));
        assertFoo("/foo:foo[foo:str='str\r\n\t']", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str\r\n\t"));
    }

    @Test
    void testSerializeEscaped() throws Exception {
        // Escaping is needed, use double quotes and escape
        assertBar("/bar:str[.=\"str'\\\"\"]", buildYangInstanceIdentifier(BAR_STR, "str'\""));
        assertBar("/bar:str[.=\"str'\\n\"]", buildYangInstanceIdentifier(BAR_STR, "str'\n"));
        assertBar("/bar:str[.=\"str'\\t\"]", buildYangInstanceIdentifier(BAR_STR, "str'\t"));
        assertBar("/bar:str[.=\"str'\r\"]", buildYangInstanceIdentifier(BAR_STR, "str'\r"));

        assertFoo("/foo:foo[foo:str=\"str'\\\"\\n\"]", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str'\"\n"));
        assertFoo("/foo:foo[foo:str=\"str'\\t\r\"]", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str'\t\r"));
    }

    @Test
    void testSerializeIdentity() throws Exception {
        assertFoo("/foo:bar[foo:qname='foo:one']", buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE));
        assertFooBar("/foo:bar[foo:qname='bar:two']", buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO));
    }

    @Test
    void testSerializeInstanceIdentifierRef() throws Exception {
        assertFooBar("/foo:baz[foo:id=\"/foo:bar[foo:qname='bar:two']\"]",
            buildYangInstanceIdentifier(FOO_BAZ, FOO_ID, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)));
    }

    @Test
    void testSerializeIdentityValue() throws Exception {
        assertBarFoo("/bar:foo[.='foo:one']", buildYangInstanceIdentifier(BAR_FOO, FOO_ONE));
        assertBar("/bar:foo[.='bar:two']", buildYangInstanceIdentifier(BAR_FOO, BAR_TWO));
    }

    @Test
    void testSerializeInstanceIdentifierValue() throws Exception {
        assertBarFoo("/bar:bar[.=\"/foo:bar[foo:qname='foo:one']\"]",
            buildYangInstanceIdentifier(BAR_BAR, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE)));
        assertBarFoo("/bar:bar[.=\"/foo:bar[foo:qname='bar:two']\"]",
            buildYangInstanceIdentifier(BAR_BAR, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)));
    }

    @Test
    void testSerializeBits() throws Exception {
        assertFoo("/foo:bee[foo:bts='']", buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of()));
        assertFoo("/foo:bee[foo:bts='one']", buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of("one")));
        assertFoo("/foo:bee[foo:bts='two three']",
            buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of("two", "three")));
    }

    @Test
    void testSerializeBitsValue() throws Exception {
        assertBar("/bar:bee[.='']", buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of()));
        assertBar("/bar:bee[.='one']", buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of("one")));
        assertBar("/bar:bee[.='two three']", buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of("two", "three")));
    }

    private static void assertFoo(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var context = mock(NamespaceContext.class);
        doReturn("foons").when(context).getNamespaceURI("foo");
        assertSerdes(context, expected, id);
    }

    private static void assertFooBar(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var context = mock(NamespaceContext.class);
        doReturn("foons").when(context).getNamespaceURI("foo");
        doReturn("barns").when(context).getNamespaceURI("bar");
        assertSerdes(context, expected, id);
    }

    private static void assertBar(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var context = mock(NamespaceContext.class);
        doReturn("barns").when(context).getNamespaceURI("bar");
        assertSerdes(context, expected, id);
    }

    private static void assertBarFoo(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var context = mock(NamespaceContext.class);
        doReturn("barns").when(context).getNamespaceURI("bar");
        doReturn("foons").when(context).getNamespaceURI("foo");
        assertSerdes(context, expected, id);
    }

    private static void assertSerdes(final NamespaceContext context, final String expected,
            final YangInstanceIdentifier id) throws Exception {
        final var writer = mock(XMLStreamWriter.class);
        final var captor = ArgumentCaptor.forClass(String.class);
        doNothing().when(writer).writeCharacters(captor.capture());
        CODEC.writeValue(writer, id);
        assertEquals(expected, captor.getValue());

        assertEquals(id, CODEC.parseValue(context, expected));
    }

    private static YangInstanceIdentifier buildYangInstanceIdentifier(final QName node, final QName key,
            final Object value) {
        return YangInstanceIdentifier.create(
                new NodeIdentifier(node), NodeIdentifierWithPredicates.of(node, key, value));
    }

    private static YangInstanceIdentifier buildYangInstanceIdentifier(final QName node, final Object value) {
        return YangInstanceIdentifier.create(new NodeIdentifier(node), new NodeWithValue<>(node, value));
    }
}
