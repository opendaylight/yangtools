/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.Bits;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
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

    private static final Map<String, Integer> BITS_MAP = Map.of("one", 0, "two", 1, "three", 2);

    private static XmlCodec<YangInstanceIdentifier> CODEC;

    @BeforeAll
    static void beforeAll() {
        final var modelContext = YangParserTestUtils.parseYang("""
            module bar {
              namespace barns;
              prefix b;

              import foo {
                prefix foo;
              }

              identity two {
                base foo:one;
              }

              leaf-list str {
                type string;
              }

              leaf-list foo {
                type identityref {
                  base foo:one;
                }
              }

              leaf-list bar {
                type instance-identifier;
              }

              leaf-list bee {
                type foo:bitz;
              }

              leaf baz {
                type instance-identifier;
              }
            }""", """
            module foo {
              namespace foons;
              prefix f;
              identity one;

              typedef bitz {
                type bits {
                  bit one;
                  bit two;
                  bit three;
                }
              }

              list foo {
                key str;
                leaf str {
                  type string;
                }
              }

              list bar {
                key qname;
                leaf qname {
                  type identityref {
                    base one;
                  }
                }
              }

              list baz {
                key id;
                leaf id {
                  type instance-identifier;
                }
              }

              list bee {
                key bts;
                leaf bts {
                  type bitz;
                }
              }
            }""");
        final var baz = assertInstanceOf(ListSchemaNode.class, modelContext.getDataChildByName(FOO_BAZ));
        final var id = assertInstanceOf(LeafSchemaNode.class, baz.getDataChildByName(FOO_ID));
        final var type = assertInstanceOf(InstanceIdentifierTypeDefinition.class, id.getType());
        CODEC = XmlCodecFactory.create(modelContext, true).instanceIdentifierCodec(type);
    }

    @AfterAll
    static void afterAll() {
        CODEC = null;
    }

    @Test
    void testSerializeSimple() throws Exception {
        // No escaping needed, use single quotes
        assertBar("/b:str[.='str\"']", createIdentifier(BAR_STR, "str\""));
        assertBar("/b:str[.='str\\']", createIdentifier(BAR_STR, "str\\"));
        assertBar("/b:str[.='str\r']", createIdentifier(BAR_STR, "str\r"));
        assertBar("/b:str[.='str\n']", createIdentifier(BAR_STR, "str\n"));
        assertBar("/b:str[.='str\t']", createIdentifier(BAR_STR, "str\t"));

        assertFoo("/f:foo[f:str='str\"\\']", createIdentifier(FOO_FOO, FOO_STR, "str\"\\"));
        assertFoo("/f:foo[f:str='str\r\n\t']", createIdentifier(FOO_FOO, FOO_STR, "str\r\n\t"));
    }

    @Test
    void testSerializeEscaped() throws Exception {
        // Escaping is needed, use double quotes and escape
        assertBar("/b:str[.=\"str'\\\"\"]", createIdentifier(BAR_STR, "str'\""));
        assertBar("/b:str[.=\"str'\\n\"]", createIdentifier(BAR_STR, "str'\n"));
        assertBar("/b:str[.=\"str'\\t\"]", createIdentifier(BAR_STR, "str'\t"));
        assertBar("/b:str[.=\"str'\r\"]", createIdentifier(BAR_STR, "str'\r"));

        assertFoo("/f:foo[f:str=\"str'\\\"\\n\"]", createIdentifier(FOO_FOO, FOO_STR, "str'\"\n"));
        assertFoo("/f:foo[f:str=\"str'\\t\r\"]", createIdentifier(FOO_FOO, FOO_STR, "str'\t\r"));
    }

    @Test
    void testSerializeIdentity() throws Exception {
        assertFoo("/f:bar[f:qname='f:one']", createIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE));
        assertFooBar("/f:bar[f:qname='b:two']", createIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO));
    }

    @Test
    void testSerializeInstanceIdentifierRef() throws Exception {
        assertFooBar("/f:baz[f:id=\"/f:bar[f:qname='b:two']\"]",
            createIdentifier(FOO_BAZ, FOO_ID, createIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)));
    }

    @Test
    void testSerializeIdentityValue() throws Exception {
        assertFooBar("/b:foo[.='f:one']", createIdentifier(BAR_FOO, FOO_ONE));
        assertBar("/b:foo[.='b:two']", createIdentifier(BAR_FOO, BAR_TWO));
    }

    @Test
    void testSerializeInstanceIdentifierValue() throws Exception {
        assertFooBar("/b:bar[.=\"/f:bar[f:qname='f:one']\"]",
            createIdentifier(BAR_BAR, createIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE)));
        assertFooBar("/b:bar[.=\"/f:bar[f:qname='b:two']\"]",
            createIdentifier(BAR_BAR, createIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)));
    }

    @Test
    void testSerializeBits() throws Exception {
        assertFoo("/f:bee[f:bts='']", createIdentifier(FOO_BEE, FOO_BTS, Bits.of(BITS_MAP, Set.of())));
        assertFoo("/f:bee[f:bts='one']", createIdentifier(FOO_BEE, FOO_BTS, Bits.of(BITS_MAP, Set.of("one"))));
        assertFoo("/f:bee[f:bts='two three']",
            createIdentifier(FOO_BEE, FOO_BTS, Bits.of(BITS_MAP, Set.of("two", "three"))));
    }

    @Test
    void testSerializeBitsValue() throws Exception {
        assertBar("/b:bee[.='']", createIdentifier(BAR_BEE, Bits.of(BITS_MAP, Set.of())));
        assertBar("/b:bee[.='one']", createIdentifier(BAR_BEE, Bits.of(BITS_MAP, Set.of("one"))));
        assertBar("/b:bee[.='two three']", createIdentifier(BAR_BEE, Bits.of(BITS_MAP, Set.of("two", "three"))));
    }

    private static void assertBar(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var writer = mockWriter();
        doNothing().when(writer).writeNamespace("b", "barns");

        final var reader = mock(NamespaceContext.class);
        doReturn("barns").when(reader).getNamespaceURI("b");
        assertSerdes(reader, writer, expected, id);
    }

    private static void assertFoo(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var writer = mockWriter();
        doNothing().when(writer).writeNamespace("f", "foons");

        final var reader = mock(NamespaceContext.class);
        doReturn("foons").when(reader).getNamespaceURI("f");

        assertSerdes(reader, writer, expected, id);
    }

    private static void assertFooBar(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var writer = mockWriter();
        doNothing().when(writer).writeNamespace("f", "foons");
        doNothing().when(writer).writeNamespace("b", "barns");

        final var reader = mock(NamespaceContext.class);
        doReturn("foons").when(reader).getNamespaceURI("f");
        doReturn("barns").when(reader).getNamespaceURI("b");

        assertSerdes(reader, writer, expected, id);
    }

    private static void assertSerdes(final NamespaceContext reader, final XMLStreamWriter writer,
            final String expected, final YangInstanceIdentifier id) throws Exception {
        assertEquals(id, CODEC.parseValue(reader, expected));

        final var captor = ArgumentCaptor.forClass(String.class);
        doNothing().when(writer).writeCharacters(captor.capture());
        CODEC.writeValue(writer, id);
        assertEquals(expected, captor.getValue());
    }

    private static XMLStreamWriter mockWriter() {
        final var writer = mock(XMLStreamWriter.class);
        doReturn(null).when(writer).getNamespaceContext();
        return writer;
    }

    private static YangInstanceIdentifier createIdentifier(final QName node, final QName key, final Object value) {
        return YangInstanceIdentifier.builder().node(node).nodeWithKey(node, key, value).build();
    }

    private static YangInstanceIdentifier createIdentifier(final QName node, final Object value) {
        return YangInstanceIdentifier.builder().node(node).node(new NodeWithValue<>(node, value)).build();
    }
}
