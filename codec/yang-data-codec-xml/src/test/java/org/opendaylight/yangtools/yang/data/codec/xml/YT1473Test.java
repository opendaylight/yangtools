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
        final var modelContext = YangParserTestUtils.parseYang("""
            module bar {
              namespace barns;
              prefix bar;

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
              prefix foo;
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
        CODEC = XmlCodecFactory.create(modelContext).instanceIdentifierCodec(type);
    }

    @AfterAll
    static void afterAll() {
        CODEC = null;
    }

    @Test
    void testSerializeSimple() throws Exception {
        // No escaping needed, use single quotes
        assertBar("/a:str[.='str\"']", buildYangInstanceIdentifier(BAR_STR, "str\""));
        assertBar("/a:str[.='str\\']", buildYangInstanceIdentifier(BAR_STR, "str\\"));
        assertBar("/a:str[.='str\r']", buildYangInstanceIdentifier(BAR_STR, "str\r"));
        assertBar("/a:str[.='str\n']", buildYangInstanceIdentifier(BAR_STR, "str\n"));
        assertBar("/a:str[.='str\t']", buildYangInstanceIdentifier(BAR_STR, "str\t"));

        assertFoo("/a:foo[a:str='str\"\\']", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str\"\\"));
        assertFoo("/a:foo[a:str='str\r\n\t']", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str\r\n\t"));
    }

    @Test
    void testSerializeEscaped() throws Exception {
        // Escaping is needed, use double quotes and escape
        assertBar("/a:str[.=\"str'\\\"\"]", buildYangInstanceIdentifier(BAR_STR, "str'\""));
        assertBar("/a:str[.=\"str'\\n\"]", buildYangInstanceIdentifier(BAR_STR, "str'\n"));
        assertBar("/a:str[.=\"str'\\t\"]", buildYangInstanceIdentifier(BAR_STR, "str'\t"));
        assertBar("/a:str[.=\"str'\r\"]", buildYangInstanceIdentifier(BAR_STR, "str'\r"));

        assertFoo("/a:foo[a:str=\"str'\\\"\\n\"]", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str'\"\n"));
        assertFoo("/a:foo[a:str=\"str'\\t\r\"]", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str'\t\r"));
    }

    @Test
    void testSerializeIdentity() throws Exception {
        assertFoo("/a:bar[a:qname='a:one']", buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE));
        assertFooBar("/a:bar[a:qname='b:two']", buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO));
    }

    @Test
    void testSerializeInstanceIdentifierRef() throws Exception {
        assertFooBar("/a:baz[a:id=\"/a:bar[a:qname='b:two']\"]",
            buildYangInstanceIdentifier(FOO_BAZ, FOO_ID, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)));
    }

    @Test
    void testSerializeIdentityValue() throws Exception {
        assertBarFoo("/a:foo[.='b:one']", buildYangInstanceIdentifier(BAR_FOO, FOO_ONE));
        assertBar("/a:foo[.='a:two']", buildYangInstanceIdentifier(BAR_FOO, BAR_TWO));
    }

    @Test
    void testSerializeInstanceIdentifierValue() throws Exception {
        assertBarFoo("/a:bar[.=\"/b:bar[b:qname='b:one']\"]",
            buildYangInstanceIdentifier(BAR_BAR, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE)));
        assertBarFoo("/a:bar[.=\"/b:bar[b:qname='a:two']\"]",
            buildYangInstanceIdentifier(BAR_BAR, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)));
    }

    @Test
    void testSerializeBits() throws Exception {
        assertFoo("/a:bee[a:bts='']", buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of()));
        assertFoo("/a:bee[a:bts='one']", buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of("one")));
        assertFoo("/a:bee[a:bts='two three']",
            buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of("two", "three")));
    }

    @Test
    void testSerializeBitsValue() throws Exception {
        assertBar("/a:bee[.='']", buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of()));
        assertBar("/a:bee[.='one']", buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of("one")));
        assertBar("/a:bee[.='two three']", buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of("two", "three")));
    }

    private static void assertFoo(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var context = mock(NamespaceContext.class);
        doReturn("foons").when(context).getNamespaceURI("a");
        assertSerdes(context, expected, id);
    }

    private static void assertFooBar(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var context = mock(NamespaceContext.class);
        doReturn("foons").when(context).getNamespaceURI("a");
        doReturn("barns").when(context).getNamespaceURI("b");
        assertSerdes(context, expected, id);
    }

    private static void assertBar(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var context = mock(NamespaceContext.class);
        doReturn("barns").when(context).getNamespaceURI("a");
        assertSerdes(context, expected, id);
    }

    private static void assertBarFoo(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var context = mock(NamespaceContext.class);
        doReturn("barns").when(context).getNamespaceURI("a");
        doReturn("foons").when(context).getNamespaceURI("b");
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
        return YangInstanceIdentifier.of(new NodeIdentifier(node), NodeIdentifierWithPredicates.of(node, key, value));
    }

    private static YangInstanceIdentifier buildYangInstanceIdentifier(final QName node, final Object value) {
        return YangInstanceIdentifier.of(new NodeIdentifier(node), new NodeWithValue<>(node, value));
    }
}
