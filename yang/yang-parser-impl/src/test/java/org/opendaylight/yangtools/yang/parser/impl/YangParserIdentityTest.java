package org.opendaylight.yangtools.yang.parser.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public class YangParserIdentityTest {

    @Test(expected = YangParseException.class)
    public void testParsingIdentityTestModuleShouldThrowYangParseException()
            throws IOException, URISyntaxException {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource(
                "/identity").toURI());
    }

}
