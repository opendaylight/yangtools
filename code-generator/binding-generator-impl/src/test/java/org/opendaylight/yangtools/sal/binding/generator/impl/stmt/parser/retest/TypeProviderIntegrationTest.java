/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl.stmt.parser.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public class TypeProviderIntegrationTest {
    private final String PKG = "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.";
    private static SchemaContext context;
    private TypeProviderImpl provider;
    private Module m;

    @BeforeClass
    public static void setup() throws Exception {
        File abstractTopology = new File(TypeProviderIntegrationTest.class.getResource("/type-provider/test.yang")
                .toURI());
        File ietfInetTypes = new File(TypeProviderIntegrationTest.class.getResource("/ietf/ietf-inet-types.yang")
                .toURI());
        context = RetestUtils.parseYangSources(abstractTopology, ietfInetTypes);
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
        assertEquals(PKG + "MyEnumeration.Seven", actual);
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

    @Test
    public void testGetTypeDefaultConstructionUnion() throws ParseException {
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName("leaf-union");
        String actual = provider.getTypeDefaultConstruction(leaf);
        String expected = "new " + PKG + "TestData.LeafUnion(\"111\".toCharArray())";
        assertEquals(expected, actual);

        leaf = (LeafSchemaNode) m.getDataChildByName("ext-union");
        actual = provider.getTypeDefaultConstruction(leaf);
        expected = "new " + PKG + "MyUnion(\"111\".toCharArray())";
        assertEquals(expected, actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUnionNested() throws ParseException {
        ContainerSchemaNode c1 = (ContainerSchemaNode) m.getDataChildByName("c1");
        ContainerSchemaNode c2 = (ContainerSchemaNode) c1.getDataChildByName("c2");
        ContainerSchemaNode c3 = (ContainerSchemaNode) c2.getDataChildByName("c3");
        LeafSchemaNode leaf = (LeafSchemaNode) c3.getDataChildByName("id");

        String actual = provider.getTypeDefaultConstruction(leaf);
        String expected = "new " + PKG + "NestedUnion(\"111\".toCharArray())";
        assertEquals(expected, actual);
    }

    @Test
    public void testGetParamNameFromType() throws ParseException {
        m = context.findModuleByName("ietf-inet-types", new SimpleDateFormat("yyyy-MM-dd").parse("2010-09-24"));
        Set<TypeDefinition<?>> types = m.getTypeDefinitions();
        TypeDefinition<?> ipv4 = null;
        TypeDefinition<?> ipv6 = null;
        TypeDefinition<?> ipv4Pref = null;
        TypeDefinition<?> ipv6Pref = null;
        for (TypeDefinition<?> type : types) {
            if ("ipv4-address".equals(type.getQName().getLocalName())) {
                ipv4 = type;
            } else if ("ipv6-address".equals(type.getQName().getLocalName())) {
                ipv6 = type;
            } else if ("ipv4-prefix".equals(type.getQName().getLocalName())) {
                ipv4Pref = type;
            } else if ("ipv6-prefix".equals(type.getQName().getLocalName())) {
                ipv6Pref = type;
            }
        }

        assertNotNull(ipv4);
        assertNotNull(ipv6);
        assertNotNull(ipv4Pref);
        assertNotNull(ipv6Pref);
        assertEquals("ipv4Address", provider.getParamNameFromType(ipv4));
        assertEquals("ipv6Address", provider.getParamNameFromType(ipv6));
        assertEquals("ipv4Prefix", provider.getParamNameFromType(ipv4Pref));
        assertEquals("ipv6Prefix", provider.getParamNameFromType(ipv6Pref));
    }
}