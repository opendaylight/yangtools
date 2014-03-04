package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.binding.generator.util.ListKeyConstants.KEY_FIELD_NAME;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class ListKeyGetterAndListSubnodeGetterEqualsNameTest {

    private static List<File> testModels = null;

    @Before
    public void loadTestResources() throws URISyntaxException {
        URI folderPath = IdentityrefTypeTest.class.getResource("/list/list-key-vs-list-subnode-equal-names.yang")
                .toURI();
        File folderFile = new File(folderPath);
        testModels = new ArrayList<File>();

        if (folderFile.isFile()) {
            testModels.add(folderFile);
        } else {
            for (File file : folderFile.listFiles()) {
                if (file.isFile()) {
                    testModels.add(file);
                }
            }
        }
    }

    @Test
    public void binaryTypeTest() {
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull("context is null", context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> types = bindingGen.generateTypes(context);

        GeneratedType lstGenType = null;
        for (Type type : types) {
            if (type instanceof GeneratedType) {
                GeneratedType genType = (GeneratedType) type;
                if (genType.getName().equals("Lst")) {
                    lstGenType = genType;
                    break;
                }
            }
        }

        assertNotNull(lstGenType);

        boolean keyMethodFound = false;
        boolean getKey1MethodFound = false;
        boolean getKeyMethodFound = false;
        for (MethodSignature signature : lstGenType.getMethodDefinitions()) {
            if (signature.getName().equals(KEY_FIELD_NAME)) {
                keyMethodFound = true;
            } else if (signature.getName().equals("getKey1")) {
                getKey1MethodFound = true;
            } else if (signature.getName().equals("getKey")) {
                getKeyMethodFound = true;
            }
        }
        assertTrue(keyMethodFound);
        assertTrue(getKey1MethodFound);
        assertTrue(getKeyMethodFound);

    }
}
