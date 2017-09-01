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

import java.io.IOException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;

public class YangModelDependencyInfoTest {

    @Test
    public void testModuleWithNoImports() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info = YangModelDependencyInfo.forResource(getClass(),
            "/ietf/ietf-inet-types@2010-09-24.yang");
        assertNotNull(info);
        assertEquals("ietf-inet-types", info.getName());
        assertEquals("2010-09-24", info.getFormattedRevision());
        assertNotNull(info.getDependencies());

        assertTrue(info.equals(info));
    }

    @Test
    public void testModuleWithImports() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info = YangModelDependencyInfo.forResource(getClass(),
                "/parse-methods/dependencies/m2@2013-30-09.yang");
        assertNotNull(info);
        assertEquals("m2", info.getName());
        assertEquals("2013-30-09", info.getFormattedRevision());
        assertNotNull(info.getDependencies());
        assertEquals(2, info.getDependencies().size());
    }

    @Test
    public void testModuleWithoutRevision() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info = YangModelDependencyInfo.forResource(getClass(),
                "/no-revision/module-without-revision.yang");
        assertNotNull(info);
        assertEquals("module-without-revision", info.getName());
        assertNull(info.getFormattedRevision());
    }

    @Test
    public void testEquals() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info1 = YangModelDependencyInfo.forResource(getClass(),
            "/ietf/ietf-inet-types@2010-09-24.yang");
        YangModelDependencyInfo info2 = YangModelDependencyInfo.forResource(getClass(),
            "/no-revision/module-without-revision.yang");

        assertTrue(info1.equals(info1));
        assertFalse(info1.equals(null));
        assertFalse(info1.equals(info2));
    }

    @Test
    public void testHashcode() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info = YangModelDependencyInfo.forResource(getClass(),
                "/no-revision/module-without-revision.yang");
        assertNotEquals("hashcode", 31, info.hashCode());
    }
}
