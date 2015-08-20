package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.junit.Test;

public class ModuleSourceTest {

    @Test
    public void test() throws SourceException, ReactorException, URISyntaxException, IOException {
        SchemaContext schema = StmtTestUtils.parseYangSources("/module-source");

        assertNotNull(schema);

        Set<Module> modules = schema.getModules();
        assertNotNull(modules);
        assertEquals(1,modules.size());

        Module simpleModule = modules.iterator().next();
        String source = simpleModule.getSource();
        String moduleSourcePath = simpleModule.getModuleSourcePath();

        File simpleYang = new File(getClass().getResource("/module-source/simple-module.yang").toURI());

        assertEquals(simpleYang.getPath(), moduleSourcePath);
        assertEquals(readFile(moduleSourcePath), source);
    }

    private String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
}
