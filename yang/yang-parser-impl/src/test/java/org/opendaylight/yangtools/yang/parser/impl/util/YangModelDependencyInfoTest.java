package org.opendaylight.yangtools.yang.parser.impl.util;

import static org.junit.Assert.*;

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
    }
    
    
    @Test
    public void testModuleWithImports() {
        InputStream stream = getClass().getResourceAsStream("/parse-methods/dependencies/m2@2013-30-09.yang");
        YangModelDependencyInfo info = YangModelDependencyInfo.fromInputStream(stream);
        assertNotNull(info);
        assertEquals("m2", info.getName());
        assertEquals("2013-30-09", info.getFormattedRevision());
        assertNotNull(info.getDependencies());
        assertEquals(2, info.getDependencies().size());
    }

}
