/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableSet;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

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
    private static final QName BAR_STR = QName.create(BAR_NS, "str"); // leaf-list of type 'string'
    private static final QName BAR_FOO = QName.create(BAR_NS, "foo"); // leaf-list of type 'foo:one' based
    private static final QName BAR_BAR = QName.create(BAR_NS, "bar"); // leaf-list of type 'instance-identifier'
    private static final QName BAR_BEE = QName.create(BAR_NS, "bee"); // leaf-list of type 'foo:bts' (bits)
    private static final QName BAR_TWO = QName.create(BAR_NS, "two"); // identity inheriting 'foo:one'

    private static JSONCodec<YangInstanceIdentifier> CODEC;

    @BeforeAll
    static void beforeAll() {
        final var modelContext = YangParserTestUtils.parseYang("""
            module bar {
              namespace barns;
              prefix bar;
              import foo { prefix foo; }

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
        CODEC = JSONCodecFactorySupplier.RFC7951.getShared(modelContext).instanceIdentifierCodec(type);
    }

    @AfterAll
    static void afterAll() {
        CODEC = null;
    }

    @Test
    void testSerializeSimple() throws Exception {
        // No escaping needed, use single quotes
        assertSerdes("/bar:str[.='str\"']", buildYangInstanceIdentifier(BAR_STR, "str\""));
        assertSerdes("/bar:str[.='str\\']", buildYangInstanceIdentifier(BAR_STR, "str\\"));
        assertSerdes("/bar:str[.='str\r']", buildYangInstanceIdentifier(BAR_STR, "str\r"));
        assertSerdes("/bar:str[.='str\n']", buildYangInstanceIdentifier(BAR_STR, "str\n"));
        assertSerdes("/bar:str[.='str\t']", buildYangInstanceIdentifier(BAR_STR, "str\t"));

        assertSerdes("/foo:foo[str='str\"\\']", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str\"\\"));
        assertSerdes("/foo:foo[str='str\r\n\t']", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str\r\n\t"));
    }

    @Test
    void testSerializeEscaped() throws Exception {
        // Escaping is needed, use double quotes and escape
        assertSerdes("/bar:str[.=\"str'\\\"\"]", buildYangInstanceIdentifier(BAR_STR, "str'\""));
        assertSerdes("/bar:str[.=\"str'\\n\"]", buildYangInstanceIdentifier(BAR_STR, "str'\n"));
        assertSerdes("/bar:str[.=\"str'\\t\"]", buildYangInstanceIdentifier(BAR_STR, "str'\t"));
        assertSerdes("/bar:str[.=\"str'\r\"]", buildYangInstanceIdentifier(BAR_STR, "str'\r"));

        assertSerdes("/foo:foo[str=\"str'\\\"\\n\"]", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str'\"\n"));
        assertSerdes("/foo:foo[str=\"str'\\t\r\"]", buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str'\t\r"));
    }

    @Test
    void testSerializeIdentityRefSame() throws Exception {
        assertSerdes("/foo:bar[qname='one']", buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE));
    }

    @Test
    void testSerializeIdentityRefOther() throws Exception {
        // No escaping is needed, use double quotes and escape
        assertSerdes("/foo:bar[qname='bar:two']", buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO));
    }

    @Test
    void testSerializeInstanceIdentifierRef() throws Exception {
        assertSerdes("/foo:baz[id=\"/foo:bar[qname='bar:two']\"]",
            buildYangInstanceIdentifier(FOO_BAZ, FOO_ID, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)));
    }

    @Test
    void testSerializeIdentityValue() throws Exception {
        assertSerdes("/bar:foo[.='foo:one']", buildYangInstanceIdentifier(BAR_FOO, FOO_ONE));
        assertSerdes("/bar:foo[.='two']", buildYangInstanceIdentifier(BAR_FOO, BAR_TWO));
    }

    @Test
    void testSerializeInstanceIdentifierValue() throws Exception {
        assertSerdes("/bar:bar[.=\"/foo:bar[qname='bar:two']\"]",
            buildYangInstanceIdentifier(BAR_BAR, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)));
        assertSerdes("/bar:bar[.=\"/foo:bar[qname='one']\"]",
            buildYangInstanceIdentifier(BAR_BAR, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE)));
    }

    @Test
    void testSerializeBits() throws Exception {
        assertSerdes("/foo:bee[bts='']", buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of()));
        assertSerdes("/foo:bee[bts='one']", buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of("one")));
        assertSerdes("/foo:bee[bts='two three']",
            buildYangInstanceIdentifier(FOO_BEE, FOO_BTS, ImmutableSet.of("two", "three")));
    }

    @Test
    void testSerializeBitsValue() throws Exception {
        assertSerdes("/bar:bee[.='']", buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of()));
        assertSerdes("/bar:bee[.='one']", buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of("one")));
        assertSerdes("/bar:bee[.='two three']", buildYangInstanceIdentifier(BAR_BEE, ImmutableSet.of("two", "three")));
    }

    private static void assertSerdes(final String expected, final YangInstanceIdentifier id) throws Exception {
        final var writer = mock(JsonWriter.class);
        final var captor = ArgumentCaptor.forClass(String.class);
        doReturn(writer).when(writer).value(anyString());
        CODEC.writeValue(writer, id);
        verify(writer).value(captor.capture());

        assertEquals(expected, captor.getValue());
        assertEquals(id, CODEC.parseValue(expected));
    }

    private static YangInstanceIdentifier buildYangInstanceIdentifier(final QName node, final QName key,
            final Object value) {
        return YangInstanceIdentifier.of(new NodeIdentifier(node), NodeIdentifierWithPredicates.of(node, key, value));
    }

    private static YangInstanceIdentifier buildYangInstanceIdentifier(final QName node, final Object value) {
        return YangInstanceIdentifier.of(new NodeIdentifier(node), new NodeWithValue<>(node, value));
    }
}
