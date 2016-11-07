/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

public class YinFileIdentityStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, modules.size());
    }

    @Test
    public void testIdentity() throws URISyntaxException {
        Module testModule = TestUtils.findModule(modules, "config");
        assertNotNull(testModule);

        Set<IdentitySchemaNode> identities = testModule.getIdentities();
        assertEquals(2, identities.size());

        Iterator<IdentitySchemaNode> idIterator = identities.iterator();
        IdentitySchemaNode id = idIterator.next();

        assertThat(id.getQName().getLocalName(), anyOf(is("module-type"), is("service-type")));
        assertTrue(id.getBaseIdentities().isEmpty());

        id = idIterator.next();
        assertThat(id.getQName().getLocalName(), anyOf(is("module-type"), is("service-type")));
        assertTrue(id.getBaseIdentities().isEmpty());

    }
}
