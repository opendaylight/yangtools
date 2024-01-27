/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug8083Test {
    private static final QNameModule FOOMOD = QNameModule.of("http://example.com/foomod");
    private static final QNameModule BARMOD = QNameModule.of("http://example.com/barmod");

    private static final QName FOO_QNAME = QName.create(FOOMOD, "foo");
    private static final QName FOOLIST_QNAME = QName.create(FOOMOD, "foo-list");
    private static final QName NAME_QNAME = QName.create(FOOMOD, "name");
    private static final QName TOP_QNAME = QName.create(FOOMOD, "top");
    private static final QName BARCONTAINER_QNAME = QName.create(BARMOD, "bar-container");

    private static final YangInstanceIdentifier TEST_IID = YangInstanceIdentifier.builder()
            .node(TOP_QNAME)
            .node(FOOLIST_QNAME)
            .node(NodeIdentifierWithPredicates.of(FOOLIST_QNAME, ImmutableMap.of(NAME_QNAME, "key-value")))
            .node(BARCONTAINER_QNAME)
            .node(QName.create(BARMOD, "bar-leaf"))
            .build();
    private static final String BAZ_YANG = """
        module baz {
          namespace baz-ns;
          prefix baz-prefix;

          container top-cont {
            list keyed-list {
              key empty-key-leaf;
              leaf empty-key-leaf {
                type empty;
              }
              leaf regular-leaf {
                type int32;
              }
            }
            leaf iid-leaf {
              type instance-identifier;
            }
          }
        }""";
    private static final String FOOBAR_YANG = """
        module foobar {
          namespace foobar-ns;
          prefix foobar-prefix;
          container top-cont {
            list keyed-list {
              key iid-key-leaf;
              leaf iid-key-leaf {
                type instance-identifier;
              }
              leaf regular-leaf {
                type int32;
              }
            }
            leaf iid-leaf {
              type instance-identifier;
            }
            leaf leaf-b {
              type int32;
            }
          }
        }""";
    private static final String ZAB_YANG = """
        module zab {
          namespace zab-ns;
          prefix zab-prefix;
          identity base-id;
          identity derived-id {
            base base-id;
          }
          container top-cont {
            list keyed-list {
              key identityref-key-leaf;
              leaf identityref-key-leaf {
                type identityref {
                  base base-id;
                }
              }
              leaf regular-leaf {
                type int32;
              }
            }
            leaf iid-leaf {
              type instance-identifier;
            }
          }
        }""";

    private static EffectiveModelContext FULL_SCHEMA_CONTEXT;

    @BeforeAll
    static void init() {
        FULL_SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module example-barmod {
              namespace "http://example.com/barmod";
              prefix "barmod";
              import example-foomod {
                prefix "foomod";
              }
              augment "/foomod:top/foomod:foo-list" {
                container bar-container {
                  leaf bar-leaf {
                    type string;
                  }
                }
              }
            }""", BAZ_YANG, """
            module example-foomod {
              namespace "http://example.com/foomod";
              prefix "foomod";
              container top {
                leaf foo {
                  type instance-identifier;
                }
                list foo-list {
                  key name;
                  leaf name {
                    type string;
                  }
                }
              }
            }""", FOOBAR_YANG, ZAB_YANG);
    }

    @AfterAll
    static void cleanup() {
        FULL_SCHEMA_CONTEXT = null;
    }

    @Test
    void testInstanceIdentifierSerializeNew() throws IOException {
        assertEquals("/example-foomod:top/foo-list[name='key-value']/example-barmod:bar-container/bar-leaf",
            writeInstanceIdentifier(JSONCodecFactorySupplier.RFC7951));
    }

    @Test
    void testInstanceIdentifierSerializeOld() throws IOException {
        assertEquals("/example-foomod:top/example-foomod:foo-list[example-foomod:name='key-value']"
                + "/example-barmod:bar-container/example-barmod:bar-leaf",
            writeInstanceIdentifier(JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02));
    }

    @Test
    void testRFC7951InstanceIdentifierPath() throws IOException, URISyntaxException {
        final var inputJson = loadTextFile("/bug8083/json/foo.json");

        // deserialization
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.RFC7951.getShared(FULL_SCHEMA_CONTEXT));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();

        final var container = assertInstanceOf(ContainerNode.class, transformedInput);
        assertEquals(TEST_IID,
            assertInstanceOf(LeafNode.class, container.childByArg(new NodeIdentifier(FOO_QNAME))).body());
    }

    @Test
    void testInstanceIdentifierPathWithEmptyListKey() throws IOException, URISyntaxException {
        final var schemaContext = YangParserTestUtils.parseYang(BAZ_YANG);
        final var inputJson = loadTextFile("/bug8083/json/baz.json");

        // deserialization
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    void testInstanceIdentifierPathWithIdentityrefListKey() throws IOException, URISyntaxException {
        final var schemaContext = YangParserTestUtils.parseYang(ZAB_YANG);
        final var inputJson = loadTextFile("/bug8083/json/zab.json");

        // deserialization
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    void testInstanceIdentifierPathWithInstanceIdentifierListKey() throws IOException, URISyntaxException {
        final var schemaContext = YangParserTestUtils.parseYang(FOOBAR_YANG);
        final var inputJson = loadTextFile("/bug8083/json/foobar.json");

        // deserialization
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    private static JSONCodec<YangInstanceIdentifier> getCodec(final JSONCodecFactorySupplier supplier) {
        final var top = assertInstanceOf(ContainerSchemaNode.class, FULL_SCHEMA_CONTEXT.dataChildByName(TOP_QNAME));
        final var foo = assertInstanceOf(LeafSchemaNode.class, top.dataChildByName(FOO_QNAME));
        final var type = assertInstanceOf(InstanceIdentifierTypeDefinition.class, foo.getType());
        return supplier.createSimple(FULL_SCHEMA_CONTEXT).instanceIdentifierCodec(type);
    }

    private static String writeInstanceIdentifier(final JSONCodecFactorySupplier supplier) throws IOException {
        final var writer = mock(JsonWriter.class);
        final var captor = ArgumentCaptor.forClass(String.class);
        doReturn(writer).when(writer).value(captor.capture());

        getCodec(supplier).writeValue(writer, TEST_IID);
        verify(writer).value(any(String.class));
        return captor.getValue();
    }
}
