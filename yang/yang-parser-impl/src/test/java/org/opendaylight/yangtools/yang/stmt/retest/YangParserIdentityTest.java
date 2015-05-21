package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public class YangParserIdentityTest {

    // base identity name equals identity name
    @Test(expected = YangParseException.class)
    public void testParsingIdentityTestModuleShouldThrowYangParseException()
            throws IOException, URISyntaxException, SourceException, ReactorException {
        File yang = new File(getClass().getResource(
                "/identity/identitytest.yang").toURI());
        InputStream stream = new FileInputStream(yang);
        TestUtils.loadModule(stream);
    }

    // same module prefixed base identity name equals identity name
    @Test(expected = YangParseException.class)
    public void testParsingPrefixIdentityTestModuleShouldThrowYangParseException()
            throws URISyntaxException, IOException, SourceException, ReactorException {
        File yang = new File(getClass().getResource(
                "/identity/prefixidentitytest.yang").toURI());
        InputStream stream = new FileInputStream(yang);
        TestUtils.loadModule(stream);
    }

    // imported module prefixed base identity name equals identity name, but
    // prefix differs
    @Test
    public void testParsingImportPrefixIdentityTestModuleShouldThrowYangParseException()
            throws URISyntaxException, IOException, YangSyntaxErrorException, SourceException, ReactorException {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource(
                "/identity/import").toURI());
        Module module = TestUtils.findModule(modules,
                "prefiximportidentitytest");
        Set<ModuleImport> imports = module.getImports();
        assertEquals(imports.size(), 1);
        ModuleImport dummy = TestUtils.findImport(imports, "dummy");
        assertNotEquals(dummy.getPrefix(), module.getPrefix());
    }
}
