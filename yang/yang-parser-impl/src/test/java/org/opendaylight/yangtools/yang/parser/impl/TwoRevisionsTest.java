package org.opendaylight.yangtools.yang.parser.impl;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TwoRevisionsTest {

    @Test
    public void testTwoRevisions() throws Exception {
        YangModelParser parser = new YangParserImpl();

        Set<Module> modules = TestUtils.loadModules(getClass().getResource("/ietf").getPath(), parser);
        assertEquals(2, TestUtils.findModules(modules, "network-topology").size());

        SchemaContext schemaContext = parser.resolveSchemaContext(modules);
        assertEquals(2, TestUtils.findModules(schemaContext.getModules(), "network-topology").size());

    }

}
