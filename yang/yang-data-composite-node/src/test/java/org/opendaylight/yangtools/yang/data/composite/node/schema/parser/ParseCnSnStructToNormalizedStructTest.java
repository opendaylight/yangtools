/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.composite.node.schema.parser;

import static org.junit.Assert.assertEquals;

import org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.parser.CnSnToNormalizedNodeParserFactory;


import org.opendaylight.yangtools.yang.data.composite.node.schema.TestUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

public class ParseCnSnStructToNormalizedStructTest {

    private static DataSchemaNode resolvedDataSchemaNode;

    @BeforeClass
    public static void loadData() throws URISyntaxException {
        Set<Module> modules = TestUtils.loadModulesFrom("/cnsn-to-normalized-node/yang");
        Module resolvedModule = TestUtils.resolveModule("simple-container-yang", modules);
        resolvedDataSchemaNode = TestUtils.resolveDataSchemaNode("cont", resolvedModule);
    }

    @Test
    public void testCnSnToNormalizedNode() throws URISyntaxException {

        CompositeNode compNode = TestUtils.prepareCompositeNodeStruct();

        List<Node<?>> lst = new ArrayList<Node<?>>();
        lst.add(compNode);
        ContainerNode parsed = CnSnToNormalizedNodeParserFactory.getInstance().getContainerNodeParser()
                .parse(lst, (ContainerSchemaNode) resolvedDataSchemaNode);

        ContainerNode prepareExpectedStruct = TestUtils.prepareNormalizedNodeStruct();
        assertEquals(prepareExpectedStruct, parsed);
    }

}
