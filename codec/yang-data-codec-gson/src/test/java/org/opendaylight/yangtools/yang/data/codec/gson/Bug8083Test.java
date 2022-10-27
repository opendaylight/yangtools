/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8083Test {
    private static final QNameModule FOOMOD = QNameModule.create(XMLNamespace.of("http://example.com/foomod"));
    private static final QNameModule BARMOD = QNameModule.create(XMLNamespace.of("http://example.com/barmod"));

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

    private static EffectiveModelContext FULL_SCHEMA_CONTEXT;

    @BeforeClass
    public static void init() {
        FULL_SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/bug8083/yang/");
    }

    @AfterClass
    public static void cleanup() {
        FULL_SCHEMA_CONTEXT = null;
    }

    @Test
    public void testInstanceIdentifierSerializeNew() throws IOException {
        assertEquals("/example-foomod:top/foo-list[name='key-value']/example-barmod:bar-container/bar-leaf",
            writeInstanceIdentifier(JSONCodecFactorySupplier.RFC7951));
    }

    @Test
    public void testInstanceIdentifierSerializeOld() throws IOException {
        assertEquals("/example-foomod:top/example-foomod:foo-list[example-foomod:name='key-value']"
                + "/example-barmod:bar-container/example-barmod:bar-leaf",
            writeInstanceIdentifier(JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02));
    }

    @Test
    public void testRFC7951InstanceIdentifierPath() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/bug8083/json/foo.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.RFC7951.getShared(FULL_SCHEMA_CONTEXT));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode transformedInput = result.getResult();

        assertTrue(transformedInput instanceof ContainerNode);
        final ContainerNode container = (ContainerNode) transformedInput;
        final NormalizedNode child = container.childByArg(new NodeIdentifier(FOO_QNAME));
        assertTrue(child instanceof LeafNode);
        assertEquals(TEST_IID, child.body());
    }

    @Test
    public void testInstanceIdentifierPathWithEmptyListKey() throws IOException, URISyntaxException {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/baz.yang");
        final String inputJson = loadTextFile("/bug8083/json/baz.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithIdentityrefListKey() throws IOException, URISyntaxException {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/zab.yang");
        final String inputJson = loadTextFile("/bug8083/json/zab.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithInstanceIdentifierListKey() throws IOException, URISyntaxException {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/foobar.yang");
        final String inputJson = loadTextFile("/bug8083/json/foobar.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    private static JSONCodec<YangInstanceIdentifier> getCodec(final JSONCodecFactorySupplier supplier) {
        final DataSchemaNode top = FULL_SCHEMA_CONTEXT.findDataChildByName(TOP_QNAME).get();
        assertTrue(top instanceof ContainerSchemaNode);
        final DataSchemaNode foo = ((ContainerSchemaNode) top).findDataChildByName(FOO_QNAME).get();
        assertTrue(foo instanceof LeafSchemaNode);
        final TypeDefinition<? extends TypeDefinition<?>> type = ((LeafSchemaNode) foo).getType();
        assertTrue(type instanceof InstanceIdentifierTypeDefinition);
        return (JSONCodec<YangInstanceIdentifier>) supplier.createSimple(FULL_SCHEMA_CONTEXT)
                .instanceIdentifierCodec((InstanceIdentifierTypeDefinition) type);
    }

    private static String writeInstanceIdentifier(final JSONCodecFactorySupplier supplier) throws IOException {
        final JsonWriter writer = mock(JsonWriter.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        doReturn(writer).when(writer).value(captor.capture());

        getCodec(supplier).writeValue(writer, TEST_IID);
        verify(writer).value(any(String.class));
        return captor.getValue();
    }
}
