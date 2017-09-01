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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public class YinFileIncludeStmtTest {

    private SchemaContext context;

    @Before
    public void init() throws URISyntaxException, ReactorException, SAXException, IOException {
        context = TestUtils.loadYinModules(getClass().getResource
                ("/semantic-statement-parser/yin/include-belongs-to-test").toURI());
        assertEquals(1, context.getModules().size());
    }

    @Test
    public void testInclude() throws URISyntaxException {
        Module parentModule = TestUtils.findModule(context, "parent").get();
        assertNotNull(parentModule);

        assertEquals(1, parentModule.getSubmodules().size());
        Iterator<Module> submodulesIterator = parentModule.getSubmodules().iterator();

        Module childModule = submodulesIterator.next() ;
        assertNotNull(childModule);
        assertEquals("child", childModule.getName());
        assertEquals(new URI("urn:opendaylight/parent"), childModule.getNamespace());
    }
}
