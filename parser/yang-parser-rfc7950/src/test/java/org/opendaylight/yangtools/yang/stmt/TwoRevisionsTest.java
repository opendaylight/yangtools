/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;

public class TwoRevisionsTest extends AbstractYangTest {
    @Test
    public void testTwoRevisions() throws Exception {
        var it = assertEffectiveModelDir("/ietf").findModuleStatements("network-topology").iterator();
        assertTrue(it.hasNext());
        assertEquals(Revision.ofNullable("2013-10-21"), it.next().localQNameModule().getRevision());
        assertTrue(it.hasNext());
        assertEquals(Revision.ofNullable("2013-07-12"), it.next().localQNameModule().getRevision());
        assertFalse(it.hasNext());
    }
}
