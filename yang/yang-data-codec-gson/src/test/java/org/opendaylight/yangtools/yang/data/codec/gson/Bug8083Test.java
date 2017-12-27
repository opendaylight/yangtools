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
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8083Test {
    private static final QNameModule FOOMOD = QNameModule.create(URI.create("http://example.com/foomod"));
    private static final QNameModule BARMOD = QNameModule.create(URI.create("http://example.com/barmod"));

    private static final QName FOO_QNAME = QName.create(FOOMOD, "foo");
    private static final QName FOOLIST_QNAME = QName.create(FOOMOD, "foo-list");
    private static final QName NAME_QNAME = QName.create(FOOMOD, "name");
    private static final QName TOP_QNAME = QName.create(FOOMOD, "top");

    private static final QName BARCONTAINER_QNAME = QName.create(BARMOD, "bar-container");
    private static final QName BARLEAF_QNAME = QName.create(BARMOD, "bar-leaf");

    @Test
    public void testRFC7951InstanceIdentifierPath() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSources("/bug8083/yang/");
        final String inputJson = loadTextFile("/bug8083/json/foo.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.RFC7951.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();

        assertTrue(transformedInput instanceof ContainerNode);
        final ContainerNode container = (ContainerNode) transformedInput;
        final NormalizedNode<?, ?> child = container.getChild(new NodeIdentifier(FOO_QNAME)).get();
        assertTrue(child instanceof LeafNode);
        final YangInstanceIdentifier expected = YangInstanceIdentifier.builder()
                .node(TOP_QNAME)
                .node(FOOLIST_QNAME)
                .node(new NodeIdentifierWithPredicates(FOOLIST_QNAME, ImmutableMap.of(NAME_QNAME, "key-value")))
                .node(new AugmentationIdentifier(ImmutableSet.of(BARCONTAINER_QNAME)))
                .node(BARCONTAINER_QNAME)
                .node(BARLEAF_QNAME)
                .build();
        assertEquals(expected, child.getValue());
    }

    @Ignore("JSONEmptyCodec needs to be fixed first.")
    @Test
    public void testInstanceIdentifierPathWithEmptyListKey() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource("/bug8083/yang/baz.yang");
        final String inputJson = loadTextFile("/bug8083/json/baz.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithIdentityrefListKey() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource("/bug8083/yang/zab.yang");
        final String inputJson = loadTextFile("/bug8083/json/zab.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithInstanceIdentifierListKey() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource("/bug8083/yang/foobar.yang");
        final String inputJson = loadTextFile("/bug8083/json/foobar.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
