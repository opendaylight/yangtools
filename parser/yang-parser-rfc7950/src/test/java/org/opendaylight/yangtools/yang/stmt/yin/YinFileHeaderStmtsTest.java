/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.Module;

class YinFileHeaderStmtsTest extends AbstractYinModulesTest {
    @Test
    void testYinFileHeader() {
        Module testModule = context.findModules("config").iterator().next();
        assertEquals(YangVersion.VERSION_1, testModule.getYangVersion());
        assertEquals(XMLNamespace.of("urn:opendaylight:params:xml:ns:yang:controller:config"),
            testModule.getNamespace());
        assertEquals("config", testModule.getPrefix());
    }
}
