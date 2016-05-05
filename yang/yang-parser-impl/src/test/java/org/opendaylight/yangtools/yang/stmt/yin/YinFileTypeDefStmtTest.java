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

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

public class YinFileTypeDefStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, modules.size());
    }

    @Test
    public void testTypedef() throws URISyntaxException {
        Module testModule = TestUtils.findModule(modules, "config");
        assertNotNull(testModule);

        Set<TypeDefinition<?>> typeDefs = testModule.getTypeDefinitions();
        assertEquals(1, typeDefs.size());

        Iterator<TypeDefinition<?>> typeDefIterator = typeDefs.iterator();
        TypeDefinition<?> typeDef = typeDefIterator.next();
        assertEquals("service-type-ref", typeDef.getQName().getLocalName());
        assertEquals("Internal type of references to service type identity.", typeDef.getDescription());
        assertEquals("identityref", typeDef.getBaseType().getQName().getLocalName());
    }
}
