/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import org.junit.Test;

public class YangModelDependencyInfoTest {

    @Test
    public void testModuleWithNoImports() {
        InputStream stream = getClass().getResourceAsStream("/ietf/ietf-inet-types@2010-09-24.yang");
        YangModelDependencyInfo info = YangModelDependencyInfo.fromInputStream(stream);
        assertNotNull(info);
        assertEquals("ietf-inet-types", info.getName());
        assertEquals("2010-09-24", info.getFormattedRevision());
        assertNotNull(info.getDependencies());

        assertTrue(info.equals(info));
    }

    @Test
    public void testModuleWithImports() {
        InputStream stream = getClass().getResourceAsStream("/parse-methods/dependencies/m2@2013-09-30.yang");
        YangModelDependencyInfo info = YangModelDependencyInfo.fromInputStream(stream);
        assertNotNull(info);
        assertEquals("m2", info.getName());
        assertEquals("2013-09-30", info.getFormattedRevision());
        assertNotNull(info.getDependencies());
        assertEquals(2, info.getDependencies().size());
    }

    @Test
    public void testModuleWithoutRevision() {
        InputStream stream = getClass().getResourceAsStream("/no-revision/module-without-revision.yang");
        YangModelDependencyInfo info = YangModelDependencyInfo.fromInputStream(stream);
        assertNotNull(info);
        assertEquals("module-without-revision", info.getName());
        assertNull(info.getFormattedRevision());
    }

    @Test
    public void testEquals() {
        InputStream stream1 = getClass().getResourceAsStream("/ietf/ietf-inet-types@2010-09-24.yang");
        YangModelDependencyInfo info1 = YangModelDependencyInfo.fromInputStream(stream1);
        InputStream stream2 = getClass().getResourceAsStream("/no-revision/module-without-revision.yang");
        YangModelDependencyInfo info2 = YangModelDependencyInfo.fromInputStream(stream2);

        assertTrue(info1.equals(info1));
        assertFalse(info1.equals(null));
        assertFalse(info1.equals(stream1));
        assertFalse(info1.equals(info2));
    }

    @Test
    public void testHashcode() {
        InputStream stream = getClass().getResourceAsStream("/no-revision/module-without-revision.yang");
        YangModelDependencyInfo info = YangModelDependencyInfo.fromInputStream(stream);

        assertNotEquals("hashcode", 31, info.hashCode());
    }
}
