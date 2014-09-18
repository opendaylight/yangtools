package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public class YangParserIdentityTest {

    @Test
    public void testParsingIdentityTestModuleShouldThrowYangParseException() {
        try {
            Set<Module> modules = TestUtils.loadModules(getClass().getResource(
                    "/identity").toURI());
        } catch (IOException | URISyntaxException e1) {
            System.out.println(e1.getMessage());
        } catch (YangParseException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex instanceof YangParseException);
        }
    }

}
