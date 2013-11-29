/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class TypeProviderIntegrationTest {
    private final String PKG = "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.";
    private static SchemaContext context;
    private TypeProviderImpl provider;
    private Module m;

    @BeforeClass
    public static void setup() throws ParseException {
        final String path1 = TypeProviderIntegrationTest.class.getResource("/type-provider/test.yang").getPath();
        final String path2 = TypeProviderIntegrationTest.class.getResource(
                "/type-provider/ietf-inet-types@2010-09-24.yang").getPath();
        context = resolveSchemaContextFromFiles(path1, path2);
        assertNotNull(context);
    }

    @Before
    public void init() throws ParseException {
        provider = new TypeProviderImpl(context);
        m = context.findModuleByName("test", new SimpleDateFormat("yyyy-MM-dd").parse("2013-10-08"));
    }

    @Test
    public void testGetTypeDefaultConstructionBinary() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-binary");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new byte[] {77, 97, 110}", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-binary");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionBits() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-bits");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "TestData.LeafBits(false, false, true)", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-bits");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBits(false, false, true)", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionBoolean() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-boolean");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Boolean(\"true\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-boolean");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBoolean(new java.lang.Boolean(\"true\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionDecimal() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-decimal64");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"3.14\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-decimal64");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyDecimal64(new java.math.BigDecimal(\"3.14\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionEmpty() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-empty");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Boolean(\"false\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-empty");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyEmpty(new java.lang.Boolean(\"false\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionEnumeration() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-enumeration");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.LeafEnumeration.Seven", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-enumeration");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyEnumeration(" + PKG + "MyEnumeration.Seven)", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt8() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-int8");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Byte(\"11\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-int8");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt8(new java.lang.Byte(\"11\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt16() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-int16");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Short(\"111\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-int16");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt16(new java.lang.Short(\"111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt32() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-int32");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Integer(\"1111\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-int32");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt32(new java.lang.Integer(\"1111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt64() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-int64");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Long(\"11111\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-int64");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt64(new java.lang.Long(\"11111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionLeafref1() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-leafref");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"1.234\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-leafref");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"1.234\")", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionLeafref2() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-leafref1");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-leafref1");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionString() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-string");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("\"name\"", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-string");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyString(\"name\")", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint8() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-uint8");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Short(\"11\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-uint8");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint8(new java.lang.Short(\"11\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint16() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-uint16");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Integer(\"111\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-uint16");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint16(new java.lang.Integer(\"111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint32() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-uint32");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Long(\"1111\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-uint32");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint32(new java.lang.Long(\"1111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint64() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-uint64");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigInteger(\"11111\")", actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-uint64");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint64(new java.math.BigInteger(\"11111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstruction() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("ip-leaf");
        String actual = provider.getTypeDefaultConstruction(leaf);
        String exp = "new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address(\"0.0.0.1\")";
        assertEquals(exp, actual);
    }

    private static SchemaContext resolveSchemaContextFromFiles(final String... yangFiles) {
        final YangModelParser parser = new YangParserImpl();

        final List<File> inputFiles = new ArrayList<File>();
        for (int i = 0; i < yangFiles.length; ++i) {
            inputFiles.add(new File(yangFiles[i]));
        }

        final Set<Module> modules = parser.parseYangModels(inputFiles);
        return parser.resolveSchemaContext(modules);
    }

}
