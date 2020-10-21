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
import static org.junit.Assert.assertTrue;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class YangModelDependencyInfoTest {

    @Test
    public void testModuleWithNoImports() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info = forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        assertNotNull(info);
        assertEquals("ietf-inet-types", info.getName());
        assertEquals("2010-09-24", info.getFormattedRevision());
        assertNotNull(info.getDependencies());

        assertTrue(info.equals(info));
    }

    @Test
    public void testModuleWithImports() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info = forResource("/parse-methods/dependencies/m2@2013-09-30.yang");
        assertNotNull(info);
        assertEquals("m2", info.getName());
        assertEquals("2013-09-30", info.getFormattedRevision());
        assertNotNull(info.getDependencies());
        assertEquals(2, info.getDependencies().size());
    }

    @Test
    public void testModuleWithoutRevision() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info = forResource("/no-revision/module-without-revision.yang");
        assertNotNull(info);
        assertEquals("module-without-revision", info.getName());
        assertNull(info.getFormattedRevision());
    }

    @Test
    public void testEquals() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info1 = forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        YangModelDependencyInfo info2 = forResource("/no-revision/module-without-revision.yang");

        assertTrue(info1.equals(info1));
        assertFalse(info1.equals(null));
        assertFalse(info1.equals(info2));
    }

    @Test
    public void testYangtools827() throws IOException, YangSyntaxErrorException {
        // Latest revision needs to be picked up irrespective of ordering
        YangModelDependencyInfo info = forResource("/bugs/YT827/foo.yang");
        assertEquals("2014-12-24", info.getFormattedRevision());
    }

    @Test
    public void testHashcode() throws IOException, YangSyntaxErrorException {
        YangModelDependencyInfo info = forResource("/no-revision/module-without-revision.yang");
        assertNotEquals("hashcode", 31, info.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMalformedImport() throws IOException, YangSyntaxErrorException {
        forResource("/depinfo-malformed/malformed-import.yang");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMalformedImportRev() throws IOException, YangSyntaxErrorException {
        forResource("/depinfo-malformed/malformed-import-rev.yang");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMalformedModule() throws IOException, YangSyntaxErrorException {
        forResource("/depinfo-malformed/malformed-module.yang");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMalformedRev() throws IOException, YangSyntaxErrorException {
        forResource("/depinfo-malformed/malformed-rev.yang");
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from input stream containing a YANG model. This parsing does not
     * validate full YANG module, only parses header up to the revisions and imports.
     *
     * @param refClass Base search class
     * @param resourceName resource name, relative to refClass
     * @return {@link YangModelDependencyInfo}
     * @throws YangSyntaxErrorException If the resource does not pass syntactic analysis
     * @throws IOException When the resource cannot be read
     * @throws IllegalArgumentException
     *             If input stream is not valid YANG stream
     */
    @VisibleForTesting
    private static YangModelDependencyInfo forResource(final String resourceName)
            throws IOException, YangSyntaxErrorException {
        final YangStatementStreamSource source = StmtTestUtils.sourceForResource(resourceName);
        return YangModelDependencyInfo.forIR(source.rootStatement(), source.getIdentifier());
    }
}
