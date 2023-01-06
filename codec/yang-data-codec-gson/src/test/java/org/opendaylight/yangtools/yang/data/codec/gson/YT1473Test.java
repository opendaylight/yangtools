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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1473Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName BAZ = QName.create("foo", "baz");
    private static final QName STR = QName.create("foo", "str");
    private static final QName QNAME = QName.create("foo", "qname");
    private static final QName ID = QName.create("foo", "id");

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
        CODEC = JSONCodecFactorySupplier.RFC7951.getShared(modelContext).
            instanceIdentifierCodec((InstanceIdentifierTypeDefinition) type);
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
    @Ignore("YT-1473: string escaping needs to work")
    public void testSerializeEscaped() throws Exception {
        // Escaping is needed, use double quotes and escape
        assertEquals("/foo:foo[str=\"str'\\\"\"]",
            write(new NodeIdentifier(FOO), NodeIdentifierWithPredicates.of(FOO, STR, "str'\"")));
    }

    @Test
    @Ignore("YT-1473: QName values need to be recognized and properly encoded via identity codec")
    public void testSerializeIdentityRefSame() throws Exception {
        // TODO: an improvement is to use just 'one' as the namespace is the same as the leaf (see RFC7951 section 6.8)
        assertEquals("/foo:bar[qname='foo:one']",
            write(new NodeIdentifier(BAR), NodeIdentifierWithPredicates.of(BAR, QNAME,
                QName.create("foo", "one"))));
    }

    @Test
    @Ignore("YT-1473: QName values need to be recognized and properly encoded via identity codec")
    public void testSerializeIdentityRefOther() throws Exception {
        // No escaping is needed, use double quotes and escape
        assertEquals("/foo:bar[qname='bar:one']",
            write(new NodeIdentifier(BAR), NodeIdentifierWithPredicates.of(BAR, QNAME,
                QName.create("bar", "one"))));
    }

    @Test
    @Ignore("YT-1473: Instance-identifier values need to be recognized and properly encoded and escaped")
    public void testSerializeInstanceIdentifierRef() throws Exception {
        assertEquals("/foo:baz[id=\"/foo:bar[qname='bar:one']\"]",
            write(new NodeIdentifier(BAZ), NodeIdentifierWithPredicates.of(BAZ, ID, YangInstanceIdentifier.create(
                new NodeIdentifier(BAR), NodeIdentifierWithPredicates.of(BAR, QNAME, QName.create("bar", "one"))))));
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
