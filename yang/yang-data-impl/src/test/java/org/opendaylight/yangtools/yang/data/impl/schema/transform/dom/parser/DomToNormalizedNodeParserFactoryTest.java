/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Element;

public class DomToNormalizedNodeParserFactoryTest {

    @Test
    public void testFactoryInstantiation() throws ReactorException, FileNotFoundException, URISyntaxException {
        SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/foo.yang");

        DomToNormalizedNodeParserFactory factory = DomToNormalizedNodeParserFactory.getInstance(
                DomUtils.defaultValueCodecProvider(), schemaContext,
                new DomToNormalizedNodeParserFactory.BuildingStrategyProvider() {}, true);

        assertNotNull(factory);

        ToNormalizedNodeParser<Element, LeafSetEntryNode<?>, LeafListSchemaNode> leafSetEntryNodeParser =
                factory.getLeafSetEntryNodeParser();
        assertNotNull(leafSetEntryNodeParser);

        ToNormalizedNodeParser<Element, MapEntryNode, ListSchemaNode> mapEntryNodeParser =
                factory.getMapEntryNodeParser();
        assertNotNull(mapEntryNodeParser);

        ToNormalizedNodeParser<Element, UnkeyedListEntryNode, ListSchemaNode> unkeyedListEntryNodeParser =
                factory.getUnkeyedListEntryNodeParser();
        assertNotNull(unkeyedListEntryNodeParser);

        ToNormalizedNodeParser<Element, OrderedMapNode, ListSchemaNode> orderedListNodeParser =
                factory.getOrderedListNodeParser();
        assertNotNull(orderedListNodeParser);
    }
}
