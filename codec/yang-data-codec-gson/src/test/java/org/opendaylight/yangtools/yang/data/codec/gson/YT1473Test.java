/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.gson.stream.JsonWriter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1473Test {
    private static final QName FOO = QName.create("foons", "foo");
    private static final QName BAR = QName.create("foons", "bar");
    private static final QName BAZ = QName.create("foons", "baz");
    private static final QName STR = QName.create("foons", "str");
    private static final QName QNAME = QName.create("foons", "qname");
    private static final QName ID = QName.create("foons", "id");

    private static final QName FOO_LEAF = QName.create("barns", "foo");
    private static final QName BAR_LEAF = QName.create("barns", "bar");

    private static JSONCodec<YangInstanceIdentifier> CODEC;

    @BeforeClass
    public static void beforeClass() {
        final var modelContext = YangParserTestUtils.parseYangResourceDirectory("/yt1473");
        final var baz = modelContext.getDataChildByName(BAZ);
        assertTrue(baz instanceof ListSchemaNode);
        final var id = ((ListSchemaNode) baz).getDataChildByName(ID);
        assertTrue(id instanceof LeafSchemaNode);
        final var type = ((LeafSchemaNode) id).getType();
        assertTrue(type instanceof InstanceIdentifierTypeDefinition);
        CODEC = JSONCodecFactorySupplier.RFC7951.getShared(modelContext)
            .instanceIdentifierCodec((InstanceIdentifierTypeDefinition) type);
    }

    @AfterClass
    public static void afterClass() {
        CODEC = null;
    }

    @Test
    public void testSerializeSimple() throws Exception {
        // No escaping needed, use single quotes
        assertEquals("/foo:foo[str='str\"']",
            write(new NodeIdentifier(FOO), NodeIdentifierWithPredicates.of(FOO, STR, "str\"")));
    }

    @Test
    public void testSerializeEscaped() throws Exception {
        // Escaping is needed, use double quotes and escape
        assertEquals("/foo:foo[str=\"str'\\\"\"]",
            write(new NodeIdentifier(FOO), NodeIdentifierWithPredicates.of(FOO, STR, "str'\"")));
    }

    @Test
    public void testSerializeIdentityRefSame() throws Exception {
        assertEquals("/foo:bar[qname='one']",
            write(new NodeIdentifier(BAR), NodeIdentifierWithPredicates.of(BAR, QNAME, QName.create("foons", "one"))));
    }

    @Test
    public void testSerializeIdentityRefOther() throws Exception {
        // No escaping is needed, use double quotes and escape
        assertEquals("/foo:bar[qname='bar:one']",
            write(new NodeIdentifier(BAR), NodeIdentifierWithPredicates.of(BAR, QNAME, QName.create("barns", "one"))));
    }

    @Test
    public void testSerializeInstanceIdentifierRef() throws Exception {
        assertEquals("/foo:baz[id=\"/foo:bar[qname='bar:one']\"]",
            write(new NodeIdentifier(BAZ), NodeIdentifierWithPredicates.of(BAZ, ID, YangInstanceIdentifier.create(
                new NodeIdentifier(BAR), NodeIdentifierWithPredicates.of(BAR, QNAME, QName.create("barns", "one"))))));
    }

    @Test
    @Ignore("YT-1473: QName values need to be recognized and properly encoded via identity codec")
    public void testSerializeIdentityValue() throws Exception {
        assertEquals("/bar:foo[.='foo:one']",
            write(new NodeWithValue<>(FOO_LEAF, QName.create("foons", "one"))));
    }

    @Test
    @Ignore("YT-1473: Instance-identifier values need to be recognized and properly encoded and escaped")
    public void testSerializeInstanceIdentifierValue() throws Exception {
        assertEquals("/bar:bar[.=\"/foo:bar/bar[qname='barn:one'\"]']",
            write(new NodeWithValue<>(BAR_LEAF, YangInstanceIdentifier.create(
                new NodeIdentifier(BAR), NodeIdentifierWithPredicates.of(BAR, QNAME, QName.create("barns", "one"))))));

    }

    private static String write(final PathArgument... path) throws Exception {
        final var writer = mock(JsonWriter.class);
        final var captor = ArgumentCaptor.forClass(String.class);
        doReturn(writer).when(writer).value(captor.capture());

        CODEC.writeValue(writer, YangInstanceIdentifier.create(path));
        verify(writer).value(any(String.class));
        return captor.getValue();
    }
}
