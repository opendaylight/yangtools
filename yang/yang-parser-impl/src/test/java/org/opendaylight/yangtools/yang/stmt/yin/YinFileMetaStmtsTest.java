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
import java.net.URISyntaxException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public class YinFileMetaStmtsTest {

    private SchemaContext context;

    @Before
    public void init() throws URISyntaxException, ReactorException, SAXException, IOException {
        context = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, context.getModules().size());
    }

    @Test
    public void testMetaStatements() throws URISyntaxException {
        Module testModule = TestUtils.findModule(context, "ietf-netconf-monitoring").get();
        assertNotNull(testModule);

        assertEquals("IETF NETCONF (Network Configuration) Working Group", testModule.getOrganization());
        assertEquals("WG Web:   <http://tools.ietf.org/wg/netconf/>\n" +
                "WG List:  <mailto:netconf@ietf.org>\n" +
                "\n" +
                "WG Chair: Mehmet Ersue\n" +
                "        <mailto:mehmet.ersue@nsn.com>\n" +
                "\n" +
                "WG Chair: Bert Wijnen\n" +
                "        <mailto:bertietf@bwijnen.net>\n" +
                "\n" +
                "Editor:   Mark Scott\n" +
                "        <mailto:mark.scott@ericsson.com>\n" +
                "\n" +
                "Editor:   Martin Bjorklund\n" +
                "        <mailto:mbj@tail-f.com>", testModule.getContact());
        assertEquals("NETCONF Monitoring Module.\n" + "All elements in this module are read-only.\n" + "\n" +
                "Copyright (c) 2010 IETF Trust and the persons identified as\n" + "authors of the code. All rights " +
                "reserved.\n" + "\n" + "Redistribution and use in source and binary forms, with or\n" +  "without " +
                "modification, is permitted pursuant to, and subject\n" + "to the license terms contained in, the " +
                "Simplified BSD\n" + "License set forth in Section 4.c of the IETF Trust's\n" + "Legal Provisions " +
                "Relating to IETF Documents\n" + "(http://trustee.ietf.org/license-info).\n" + "\n" + "This version " +
                "of this YANG module is part of RFC 6022; see\n" + "the RFC itself for full legal notices.",
                testModule.getDescription());
    }
}
