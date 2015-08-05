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
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinFileMetaStmtsTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(10, modules.size());
    }

    @Test
    public void testMetaStatements() throws URISyntaxException {
        Module testModule = TestUtils.findModule(modules, "ietf-interfaces");
        assertNotNull(testModule);

        assertEquals("IETF NETMOD (NETCONF Data Modeling Language) Working Group", testModule.getOrganization());
        assertEquals("WG Web:   <http://tools.ietf.org/wg/netmod/>\n" +
                "WG List:  <mailto:netmod@ietf.org>\n" +
                "\n" +
                "WG Chair: David Kessens\n" +
                "        <mailto:david.kessens@nsn.com>\n" +
                "\n" +
                "WG Chair: Juergen Schoenwaelder\n" +
                "        <mailto:j.schoenwaelder@jacobs-university.de>\n" +
                "\n" +
                "Editor:   Martin Bjorklund\n" +
                "        <mailto:mbj@tail-f.com>", testModule.getContact());
        assertEquals("This module contains a collection of YANG definitions for\n" +
                "managing network interfaces.\n" +
                "\n" +
                "Copyright (c) 2013 IETF Trust and the persons identified as\n" +
                "authors of the code.  All rights reserved.\n" +
                "\n" +
                "Redistribution and use in source and binary forms, with or\n" +
                "without modification, is permitted pursuant to, and subject\n" +
                "to the license terms contained in, the Simplified BSD License\n" +
                "set forth in Section 4.c of the IETF Trust's Legal Provisions\n" +
                "Relating to IETF Documents\n" +
                "(http://trustee.ietf.org/license-info).\n" +
                "\n" +
                "This version of this YANG module is part of RFC XXXX; see\n" +
                "the RFC itself for full legal notices.", testModule.getDescription());
        assertEquals("RFC XXXX: A YANG Data Model for Interface Management", testModule.getReference());

    }
}
