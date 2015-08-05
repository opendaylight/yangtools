/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinFileIdentityStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(10, modules.size());
    }

    @Test
    public void testIdentity() throws URISyntaxException {
        Module testModule = TestUtils.findModule(modules, "config");
        assertNotNull(testModule);

        Set<IdentitySchemaNode> identities = testModule.getIdentities();
        assertEquals(2, identities.size());

        Iterator<IdentitySchemaNode> idIterator = identities.iterator();
        IdentitySchemaNode id = idIterator.next();
        assertEquals("service-type", id.getQName().getLocalName());
        assertNull(id.getBaseIdentity());
        assertEquals("Service identity base type. All service identities must be\n" +
                "derived from this type. A service type uniquely defines a single\n" +
                "atomic API contract, such as a Java interface, a set of C\n" +
                "function declarations, or similar.\n" +
                "\n" +
                "If the service type has a corresponding Java interface, the name\n" +
                "of that interface should be attached to the derived identity MUST\n" +
                "include a java-class keyword, whose name argument points to that\n" +
                "interface.", id.getDescription());

        id = idIterator.next();
        assertEquals("module-type", id.getQName().getLocalName());
        assertNull(id.getBaseIdentity());
        assertEquals("Module identity base type. All module identities must be derived\n" +
                "from this type. A module type uniquely defines a single atomic\n" +
                "component, such as an application. Each such component is assumed\n" +
                "to have its unique, stable and versioned configuration structure.", id.getDescription());
    }
}
