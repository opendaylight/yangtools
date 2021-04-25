/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class YangModelDependencyInfoTest {
    // Utility
    private static YangModelDependencyInfo forResource(final String resourceName) {
        final YangStatementStreamSource source = StmtTestUtils.sourceForResource(resourceName);
        return YangModelDependencyInfo.forIR(source.rootStatement(), source.getIdentifier());
    }

    @Test
    public void testModuleWithNoImports() {
        YangModelDependencyInfo info = forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        assertNotNull(info);
        assertEquals("ietf-inet-types", info.getName());
        assertEquals("2010-09-24", info.getFormattedRevision());
        assertNotNull(info.getDependencies());

        assertTrue(info.equals(info));
    }

    @Test
    public void testModuleWithImports() {
        YangModelDependencyInfo info = forResource("/parse-methods/dependencies/m2@2013-09-30.yang");
        assertNotNull(info);
        assertEquals("m2", info.getName());
        assertEquals("2013-09-30", info.getFormattedRevision());
        assertNotNull(info.getDependencies());
        assertEquals(2, info.getDependencies().size());
    }

    @Test
    public void testModuleWithoutRevision() {
        YangModelDependencyInfo info = forResource("/no-revision/module-without-revision.yang");
        assertNotNull(info);
        assertEquals("module-without-revision", info.getName());
        assertNull(info.getFormattedRevision());
    }

    @Test
    public void testEquals() {
        YangModelDependencyInfo info1 = forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        YangModelDependencyInfo info2 = forResource("/no-revision/module-without-revision.yang");

        assertTrue(info1.equals(info1));
        assertFalse(info1.equals(null));
        assertFalse(info1.equals(info2));
    }

    @Test
    public void testYangtools827() {
        // Latest revision needs to be picked up irrespective of ordering
        YangModelDependencyInfo info = forResource("/bugs/YT827/foo.yang");
        assertEquals("2014-12-24", info.getFormattedRevision());
    }

    @Test
    public void testHashcode() {
        YangModelDependencyInfo info = forResource("/no-revision/module-without-revision.yang");
        assertNotEquals("hashcode", 31, info.hashCode());
    }

    @Test
    public void testMalformedImport() {
        assertThrows(IllegalArgumentException.class, () -> forResource("/depinfo-malformed/malformed-import.yang"));
    }

    @Test
    public void testMalformedImportRev() {
        assertThrows(IllegalArgumentException.class, () -> forResource("/depinfo-malformed/malformed-import-rev.yang"));
    }

    @Test
    public void testMalformedModule() {
        assertThrows(IllegalArgumentException.class, () -> forResource("/depinfo-malformed/malformed-module.yang"));
    }

    @Test
    public void testMalformedRev() {
        assertThrows(IllegalArgumentException.class, () -> forResource("/depinfo-malformed/malformed-rev.yang"));
    }
}
