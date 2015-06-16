/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;

public class ModuleIdentifierTest {

    private ModuleIdentifier moduleIdentifier;
    private ModuleIdentifier moduleIdentifier2;
    private ModuleIdentifier moduleIdentifier3;
    private ModuleIdentifier moduleIdentifier4;
    private ModuleIdentifier moduleIdentifier5;

    @Before
    public void init() throws URISyntaxException {
        Optional<URI> uri = Optional.of(new URI("testURI"));
        Optional<URI> uri2 = Optional.of(new URI("testURI2"));
        Optional<Date> revision = Optional.absent();
        moduleIdentifier = new ModuleIdentifierImpl("test-modulue", uri, revision);
        moduleIdentifier2 = new ModuleIdentifierImpl("test-modulue2", uri, revision);
        moduleIdentifier3 = moduleIdentifier;
        moduleIdentifier4 = new ModuleIdentifierImpl("test-modulue", uri2, revision);
        moduleIdentifier5 = new ModuleIdentifierImpl("test-modulue", uri, revision);
    }

    @Test
    public void testGetQNameModule() {
        assertEquals(null, moduleIdentifier.getQNameModule().getRevision());
    }

    @Test
    public void testGetRevision() {
        assertEquals(null, moduleIdentifier.getRevision());
    }

    @Test
    public void testGetName() {
        assertEquals("test-modulue", moduleIdentifier.getName());
    }

    @Test
    public void getNamespace() throws URISyntaxException {
        assertEquals(new URI("testURI"), moduleIdentifier.getNamespace());
    }

    @Test
    public void toStringTest() {
        assertTrue(moduleIdentifier.toString().contains("ModuleIdentifier"));
    }

    @Test
    public void testHashCode() {
        assertFalse(moduleIdentifier.hashCode() == moduleIdentifier2.hashCode());
    }

    @Test
    public void testEquals() {
        assertTrue(moduleIdentifier.equals(moduleIdentifier3));
        assertFalse(moduleIdentifier.equals(null));
        assertFalse(moduleIdentifier.equals("test"));
        assertFalse(moduleIdentifier.equals(moduleIdentifier2));
        assertFalse(moduleIdentifier.equals(moduleIdentifier4));
        assertTrue(moduleIdentifier.equals(moduleIdentifier5));
    }
}
