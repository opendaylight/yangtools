/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Test antlr grammar capability to parse nested unknown nodes.
 */
public class Bug394Test {

    @Test
    public void testParseList() throws Exception {
        final SchemaContext context = TestUtils.loadModules(getClass().getResource("/bugs/bug394-retest").toURI());
        final Module bug394 = TestUtils.findModule(context, "bug394").get();
        final Module bug394_ext = TestUtils.findModule(context, "bug394-ext").get();

        final ContainerSchemaNode logrecords = (ContainerSchemaNode) bug394.getDataChildByName(QName.create(
                bug394.getQNameModule(), "logrecords"));
        assertNotNull(logrecords);

        final Collection<? extends UnknownSchemaNode> nodes = logrecords.getUnknownSchemaNodes();
        assertEquals(2, nodes.size());

        final Collection<? extends ExtensionDefinition> extensions = bug394_ext.getExtensionSchemaNodes();
        assertEquals(3, extensions.size());

        final Iterator<? extends UnknownSchemaNode> it = nodes.iterator();
        assertTrue(extensions.contains(it.next().getExtensionDefinition()));
        assertTrue(extensions.contains(it.next().getExtensionDefinition()));
    }
}
