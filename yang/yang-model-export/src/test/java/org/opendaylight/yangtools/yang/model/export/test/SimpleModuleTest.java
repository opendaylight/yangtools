package org.opendaylight.yangtools.yang.model.export.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.export.YinUtils;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class SimpleModuleTest  {

    private static final File TEST_MODELS;

    static {
        try {
            TEST_MODELS = new File(SimpleModuleTest.class.getResource("/yang/").toURI());
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }


    @Test
    public void testGenerateAll() throws Exception {
        final List<File> sources = Arrays.asList(TEST_MODELS.listFiles());
        for(final File model : sources) {
            testSingleFileWithDependencies(model);
        }

    }

    private void testSingleFileWithDependencies(final File model) throws Exception {
        final YangParserImpl parser = new YangParserImpl();
        final SchemaContext schemaContext = parser.parseFile(model, TEST_MODELS);
        final File outDir = new File("target/"+model.getName());
        outDir.mkdirs();
        for(final Module module : schemaContext.getModules()) {
            exportModule(schemaContext,module,outDir);
        }
    }

    private File exportModule(final SchemaContext schemaContext,final Module module, final File outDir) throws Exception {
        final File outFile = new File(outDir,YinUtils.wellFormedYinName(module.getName(),module.getRevision()));
        try(OutputStream output = new FileOutputStream(outFile)) {
            YinUtils.writeModuleToOutputStream(schemaContext,module, output);
        }
        return outFile;
    }
}
