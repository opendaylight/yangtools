/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

class YangModelDependencyInfoTest {
    // Utility
    private static YangModelDependencyInfo forResource(final String resourceName) {
        final var source = StmtTestUtils.sourceForResource(resourceName);
        return YangModelDependencyInfo.forIR(source.rootStatement(), source.getIdentifier());
    }

    @Test
    void testModuleWithNoImports() {
        final var info = forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        assertNotNull(info);
        assertEquals("ietf-inet-types", info.getName());
        assertEquals("2010-09-24", info.getFormattedRevision());
        assertNotNull(info.getDependencies());

        assertEquals(info, info);
    }

    @Test
    void testModuleWithImports() {
        final var info = forResource("/parse-methods/dependencies/m2@2013-09-30.yang");
        assertNotNull(info);
        assertEquals("m2", info.getName());
        assertEquals("2013-09-30", info.getFormattedRevision());
        assertNotNull(info.getDependencies());
        assertEquals(2, info.getDependencies().size());
    }

    @Test
    void testModuleWithoutRevision() {
        final var info = forResource("/no-revision/module-without-revision.yang");
        assertNotNull(info);
        assertEquals("module-without-revision", info.getName());
        assertNull(info.getFormattedRevision());
    }

    @Test
    void testEquals() {
        final var info1 = forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        final var info2 = forResource("/no-revision/module-without-revision.yang");

        assertEquals(info1, info1);
        assertNotEquals(null, info1);
        assertNotEquals(info1, info2);
    }

    @Test
    void testYangtools827() {
        // Latest revision needs to be picked up irrespective of ordering
        final var info = forResource("/bugs/YT827/foo.yang");
        assertEquals("2014-12-24", info.getFormattedRevision());
    }

    @Test
    void testHashcode() {
        final var info = forResource("/no-revision/module-without-revision.yang");
        assertNotEquals(31, info.hashCode(), "hashcode");
    }

    @Test
    void testMalformedImport() {
        assertThrows(IllegalArgumentException.class, () -> forResource("/depinfo-malformed/malformed-import.yang"));
    }

    @Test
    void testMalformedImportRev() {
        assertThrows(IllegalArgumentException.class, () -> forResource("/depinfo-malformed/malformed-import-rev.yang"));
    }

    @Test
    void testMalformedModule() {
        assertThrows(IllegalArgumentException.class, () -> forResource("/depinfo-malformed/malformed-module.yang"));
    }

    @Test
    void testMalformedRev() {
        assertThrows(IllegalArgumentException.class, () -> forResource("/depinfo-malformed/malformed-rev.yang"));
    }
}
