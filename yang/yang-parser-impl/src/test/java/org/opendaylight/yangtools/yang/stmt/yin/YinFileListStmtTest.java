/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public class YinFileListStmtTest {

    private SchemaContext context;

    @Before
    public void init() throws URISyntaxException, ReactorException, SAXException, IOException {
        context = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, context.getModules().size());
    }

    @Test
    public void testListAndLeaves() {
        final Module testModule = TestUtils.findModule(context, "config").get();
        assertNotNull(testModule);

        final ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(QName.create(
                testModule.getQNameModule(), "modules"));
        assertNotNull(container);

        final ListSchemaNode list = (ListSchemaNode) container.getDataChildByName(QName.create(testModule.getQNameModule(),
                "module"));
        assertNotNull(list);
        final List<QName> keys = list.getKeyDefinition();
        assertEquals(1, keys.size());
        assertEquals("name", keys.get(0).getLocalName());

        final Collection<DataSchemaNode> children = list.getChildNodes();
        assertEquals(4, children.size());

        final Iterator<DataSchemaNode> childrenIterator = children.iterator();
        LeafSchemaNode leaf = (LeafSchemaNode) childrenIterator.next();
        assertEquals("name", leaf.getQName().getLocalName());
        assertEquals("Unique module instance name", leaf.getDescription());
        assertEquals(BaseTypes.stringType(), leaf.getType());
        assertTrue(leaf.getConstraints().isMandatory());

        leaf = (LeafSchemaNode) childrenIterator.next();
        assertEquals("type", leaf.getQName().getLocalName());

        final TypeDefinition<?> leafType = leaf.getType();
        assertTrue(leafType instanceof IdentityrefTypeDefinition);
        assertEquals("module-type", ((IdentityrefTypeDefinition)leafType).getIdentity().getQName().getLocalName());
        assertTrue(leaf.getConstraints().isMandatory());
    }

}
