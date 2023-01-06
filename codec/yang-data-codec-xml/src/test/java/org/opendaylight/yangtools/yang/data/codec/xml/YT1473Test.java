/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import javax.xml.stream.XMLStreamWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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
    private static final QName BAR_FOO = QName.create(BAR_NS, "foo"); // leaf of type 'foo:one' based
    private static final QName BAR_BAR = QName.create(BAR_NS, "bar"); // leaf of type 'instance-identifier'
    private static final QName BAR_TWO = QName.create(BAR_NS, "two"); // identity inheriting 'foo:one'

    private static XmlCodec<YangInstanceIdentifier> CODEC;

    @Mock
    private XMLStreamWriter writer;
    @Captor
    private ArgumentCaptor<String> captor;

    @BeforeAll
    public static void beforeAll() {
        final var modelContext = YangParserTestUtils.parseYangResourceDirectory("/yt1473");
        final var baz = modelContext.getDataChildByName(FOO_BAZ);
        assertTrue(baz instanceof ListSchemaNode);
        final var id = ((ListSchemaNode) baz).getDataChildByName(FOO_ID);
        assertTrue(id instanceof LeafSchemaNode);
        final var type = ((LeafSchemaNode) id).getType();
        assertTrue(type instanceof InstanceIdentifierTypeDefinition);
        CODEC = (XmlStringInstanceIdentifierCodec) XmlCodecFactory.create(modelContext)
                .instanceIdentifierCodec((InstanceIdentifierTypeDefinition) type);
    }

    @AfterAll
    public static void afterAll() {
        CODEC = null;
    }

    @Test
    public void testSerializeSimple() throws Exception {
        // No escaping needed, use single quotes
        assertEquals("/foo:foo[foo:str='str\"']", write(buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str\"")));
    }

    @Test
    @Disabled("YT-1473: string escaping needs to work")
    public void testSerializeEscaped() throws Exception {
        // Escaping is needed, use double quotes and escape
        assertEquals("/foo:foo[foo:str=\"str'\\\"\"]", write(buildYangInstanceIdentifier(FOO_FOO, FOO_STR, "str'\"")));
    }

    @Test
    @Disabled("YT-1473: QName values need to be recognized and properly encoded via identity codec")
    public void testSerializeIdentityRefSame() throws Exception {
        // TODO: an improvement is to use just 'one' as the namespace is the same as the leaf (see RFC7951 section 6.8)
        assertEquals("/foo:bar[qname='one']", write(buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, FOO_ONE)));
    }

    @Test
    @Disabled("YT-1473: QName values need to be recognized and properly encoded via identity codec")
    public void testSerializeIdentityRefOther() throws Exception {
        // No escaping is needed, use double quotes and escape
        assertEquals("/foo:bar[qname='bar:two']", write(buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)));
    }

    @Test
    @Disabled("YT-1473: Instance-identifier values need to be recognized and properly encoded and escaped")
    public void testSerializeInstanceIdentifierRef() throws Exception {
        assertEquals("/foo:baz[id=\"/foo:bar[qname='bar:two']\"]", write(
                buildYangInstanceIdentifier(FOO_BAZ, FOO_ID, buildYangInstanceIdentifier(FOO_BAR, FOO_QNAME, BAR_TWO)))
        );
    }

    @Test
    @Disabled("YT-1473: QName values need to be recognized and properly encoded via identity codec")
    public void testSerializeIdentityValue() throws Exception {
        assertEquals("/bar:foo[.='foo:one']", write(buildYangInstanceIdentifier(BAR_FOO, FOO_ONE)));
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
}
