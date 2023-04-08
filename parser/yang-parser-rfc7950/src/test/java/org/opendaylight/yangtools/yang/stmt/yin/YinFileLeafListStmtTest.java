/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public class YinFileLeafListStmtTest extends AbstractYinModulesTest {
    @Test
    public void testLeafList() {
        final var testModule = context.findModules("ietf-netconf-monitoring").iterator().next();

        final var leafList = (LeafListSchemaNode) testModule.findDataChildByName(
            QName.create(testModule.getQNameModule(), "netconf-state"),
            QName.create(testModule.getQNameModule(), "capabilities"),
            QName.create(testModule.getQNameModule(), "capability")).orElseThrow();
        assertNotNull(leafList);
        assertEquals("uri", leafList.getType().getQName().getLocalName());
        assertEquals(Optional.of("List of NETCONF capabilities supported by the server."), leafList.getDescription());
        assertFalse(leafList.isUserOrdered());
    }
}
