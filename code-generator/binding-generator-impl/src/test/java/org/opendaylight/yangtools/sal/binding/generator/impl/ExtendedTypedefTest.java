package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.yang.types.BaseYangTypes;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class ExtendedTypedefTest {

    private final static List<File> testModels = new ArrayList<File>();
    private final static String testFolderPath = AugmentedTypeTest.class.getResource("/typedef-of-typedef").getPath();

    @BeforeClass
    public static void loadTestResources() {
        final File testFolder = new File(testFolderPath);

        for (final File fileEntry : testFolder.listFiles()) {
            if (fileEntry.isFile()) {
                testModels.add(fileEntry);
            }
        }
    }

    @Test
    public void constantGenerationTest() {
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        GeneratedTransferObject simpleTypedef4 = null;
        GeneratedTransferObject extendedTypedefUnion = null;
        GeneratedTransferObject unionTypedef = null;
        GeneratedTransferObject typedefFromImport = null;
        for (final Type type : genTypes) {
            if (type instanceof GeneratedTransferObject) {
                if (type.getName().equals("SimpleTypedef4")) {
                    simpleTypedef4 = (GeneratedTransferObject) type;
                } else if (type.getName().equals("ExtendedTypedefUnion")) {
                    extendedTypedefUnion = (GeneratedTransferObject) type;
                } else if (type.getName().equals("UnionTypedef")) {
                    unionTypedef = (GeneratedTransferObject) type;
                } else if (type.getName().equals("TypedefFromImport")) {
                    typedefFromImport = (GeneratedTransferObject) type;
                }
            }
        }

        // typedef-from-import
        assertNotNull("TypedefFromImport not found", typedefFromImport);
        List<GeneratedProperty> properties = typedefFromImport.getProperties();
        assertTrue("Properties of TypedefFromImport should be empty", properties.isEmpty());
        assertEquals("TypedefFromImport should be extended", "Ipv4Address", typedefFromImport.getSuperType().getName());

        // simple-typedef4
        assertNotNull("SimpleTypedef4 not found", simpleTypedef4);
        assertNotNull("ExtendedTypedefUnion not found", extendedTypedefUnion);
        assertNotNull("UnionTypedef", unionTypedef);

        properties = simpleTypedef4.getProperties();
        assertTrue("SimpleTypedef4 shouldn't have properties.", properties.isEmpty());

        GeneratedTransferObject extendTO = simpleTypedef4.getSuperType();
        assertNotNull("SimpleTypedef4 should have extend.", extendTO);
        assertEquals("Incorrect extension for SimpleTypedef4.", "SimpleTypedef3", extendTO.getName());
        properties = extendTO.getProperties();
        assertTrue("SimpleTypedef3 shouldn't have properties.", properties.isEmpty());

        extendTO = extendTO.getSuperType();
        assertNotNull("SimpleTypedef3 should have extend.", extendTO);
        assertEquals("Incorrect extension for SimpleTypedef3.", "SimpleTypedef2", extendTO.getName());
        properties = extendTO.getProperties();
        assertTrue("SimpleTypedef2 shouldn't have properties.", properties.isEmpty());

        extendTO = extendTO.getSuperType();
        assertNotNull("SimpleTypedef2 should have extend.", extendTO);
        assertEquals("SimpleTypedef2 should be extended with SimpleTypedef1.", "SimpleTypedef1", extendTO.getName());
        properties = extendTO.getProperties();
        assertEquals("Incorrect number of properties in class SimpleTypedef1.", 1, properties.size());

        assertEquals("Incorrect property's name", "value", properties.get(0).getName());
        assertEquals("Property's incorrect type", BaseYangTypes.UINT8_TYPE, properties.get(0).getReturnType());

        extendTO = extendTO.getSuperType();
        assertNull("SimpleTypedef1 shouldn't have extend.", extendTO);

        // extended-typedef-union
        assertNotNull("ExtendedTypedefUnion object not found", extendedTypedefUnion);
        properties = extendedTypedefUnion.getProperties();
        assertTrue("ExtendedTypedefUnion shouldn't have any property", properties.isEmpty());

        extendTO = extendedTypedefUnion.getSuperType();
        assertEquals("Incorrect extension fo ExtendedTypedefUnion.", "UnionTypedef", extendTO.getName());
        assertNull("UnionTypedef shouldn't be extended", extendTO.getSuperType());
        assertEquals("Incorrect number of properties for UnionTypedef.", 4, extendTO.getProperties().size());

        GeneratedProperty simpleTypedef4Property = null;
        GeneratedProperty simpleTypedef1Property = null;
        GeneratedProperty byteTypeProperty = null;
        GeneratedProperty typedefEnumFruitProperty = null;
        for (GeneratedProperty genProperty : extendTO.getProperties()) {
            if (genProperty.getName().equals("simpleTypedef1")) {
                simpleTypedef1Property = genProperty;
            } else if (genProperty.getName().equals("simpleTypedef4")) {
                simpleTypedef4Property = genProperty;
            } else if (genProperty.getName().equals("byteType")) {
                byteTypeProperty = genProperty;
            } else if (genProperty.getName().equals("typedefEnumFruit")) {
                typedefEnumFruitProperty = genProperty;
            }
        }

        assertNotNull("simpleTypedef4 property not found in UnionTypedef", simpleTypedef4Property);
        assertNotNull("simpleTypedef1 property not found in UnionTypedef", simpleTypedef1Property);
        assertNotNull("byteType property not found in UnionTypedef", byteTypeProperty);
        assertNotNull("typedefEnumFruit property not found in UnionTypedef", typedefEnumFruitProperty);

        assertEquals("Incorrect type for property simpleTypedef4.", "SimpleTypedef4", simpleTypedef4Property
                .getReturnType().getName());
        assertEquals("Incorrect type for property simpleTypedef1.", "SimpleTypedef1", simpleTypedef1Property
                .getReturnType().getName());
        assertEquals("Incorrect type for property byteType.", "ByteType", byteTypeProperty.getReturnType().getName());
        assertEquals("Incorrect type for property typedefEnumFruit.", "TypedefEnumFruit", typedefEnumFruitProperty
                .getReturnType().getName());
    }

}
