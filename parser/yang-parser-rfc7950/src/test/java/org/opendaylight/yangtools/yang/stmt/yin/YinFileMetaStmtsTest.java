/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Module;

class YinFileMetaStmtsTest extends AbstractYinModulesTest {
    @Test
    void testMetaStatements() {
        Module testModule = context.findModules("ietf-netconf-monitoring").iterator().next();
        assertNotNull(testModule);

        assertEquals(Optional.of("IETF NETCONF (Network Configuration) Working Group"), testModule.getOrganization());
        assertEquals(Optional.of("""
            WG Web:   <http://tools.ietf.org/wg/netconf/>
            WG List:  <mailto:netconf@ietf.org>

            WG Chair: Mehmet Ersue
                    <mailto:mehmet.ersue@nsn.com>

            WG Chair: Bert Wijnen
                    <mailto:bertietf@bwijnen.net>

            Editor:   Mark Scott
                    <mailto:mark.scott@ericsson.com>

            Editor:   Martin Bjorklund
                    <mailto:mbj@tail-f.com>"""), testModule.getContact());
        assertEquals(Optional.of("""
            NETCONF Monitoring Module.
            All elements in this module are read-only.

            Copyright (c) 2010 IETF Trust and the persons identified as
            authors of the code. All rights reserved.

            Redistribution and use in source and binary forms, with or
            without modification, is permitted pursuant to, and subject
            to the license terms contained in, the Simplified BSD
            License set forth in Section 4.c of the IETF Trust's
            Legal Provisions Relating to IETF Documents
            (http://trustee.ietf.org/license-info).

            This version of this YANG module is part of RFC 6022; see
            the RFC itself for full legal notices."""),
            testModule.getDescription());
    }
}
