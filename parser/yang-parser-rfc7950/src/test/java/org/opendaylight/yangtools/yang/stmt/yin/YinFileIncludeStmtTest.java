/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

class YinFileIncludeStmtTest {
    @Test
    void testInclude() throws Exception {
        Collection<? extends Submodule> submodules = TestUtils.loadYinModules(getClass().getResource(
            "/semantic-statement-parser/yin/include-belongs-to-test").toURI()).findModules("parent").iterator().next()
            .getSubmodules();
        assertEquals(1, submodules.size());

        Submodule childModule = submodules.iterator().next();
        assertEquals("child", childModule.getName());
        assertEquals(XMLNamespace.of("urn:opendaylight/parent"), childModule.getNamespace());
    }
}
