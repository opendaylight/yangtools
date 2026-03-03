/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class YinFileIdentityStmtTest extends AbstractYinModulesTest {
    @Test
    void testIdentity() {
        var testModule = context.findModules("config").iterator().next();
        assertNotNull(testModule);

        var identities = testModule.getIdentities();
        assertEquals(2, identities.size());

        var idIterator = identities.iterator();
        var id = idIterator.next();

        assertThat(id.getQName().getLocalName(), anyOf(is("module-type"), is("service-type")));
        assertTrue(id.getBaseIdentities().isEmpty());

        id = idIterator.next();
        assertThat(id.getQName().getLocalName(), anyOf(is("module-type"), is("service-type")));
        assertTrue(id.getBaseIdentities().isEmpty());
    }
}
