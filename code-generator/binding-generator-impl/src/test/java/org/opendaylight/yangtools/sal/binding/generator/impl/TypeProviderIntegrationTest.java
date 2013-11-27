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

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class TypeProviderIntegrationTest {
    private final String PKG = "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.";

    private SchemaContext resolveSchemaContextFromFiles(final String... yangFiles) {
        final YangModelParser parser = new YangParserImpl();

        final List<File> inputFiles = new ArrayList<File>();
        for (int i = 0; i < yangFiles.length; ++i) {
            inputFiles.add(new File(yangFiles[i]));
        }

        final Set<Module> modules = parser.parseYangModels(inputFiles);
        return parser.resolveSchemaContext(modules);
    }

    @Test
    public void testGetTypeDefaultConstruction1() throws ParseException {
        final String path1 = getClass().getResource("/type-provider/test.yang").getPath();
        final String path2 = getClass().getResource("/type-provider/ietf-inet-types@2010-09-24.yang").getPath();
        final SchemaContext context = resolveSchemaContextFromFiles(path1, path2);
        assertNotNull(context);
        TypeProviderImpl provider = new TypeProviderImpl(context);
        Module m = context.findModuleByName("test", new SimpleDateFormat("yyyy-MM-dd").parse("2013-10-08"));

        LeafSchemaNode leaf = (LeafSchemaNode)m.getDataChildByName("id-binary");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new byte[] {77, 97, 110}", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-bits");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "TestData.IdBits(false, false, true)", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-boolean");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Boolean(\"true\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-decimal64");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"3.14\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-empty");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Boolean(\"false\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-enumeration");
        actual = provider.getTypeDefaultConstruction(leaf);
        //assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.IdEnumeration.Seven", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-8");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Byte(\"11\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-16");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Short(\"111\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-32");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Integer(\"1111\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-64");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Long(\"11111\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-leafref");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"1.234\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-string");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("\"name\"", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-u8");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Short(\"11\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-u16");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Integer(\"111\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-u32");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.lang.Long(\"1111\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("id-u64");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigInteger(\"11111\")", actual);
    }

    @Ignore
    @Test
    public void testGetTypeDefaultConstruction2() throws ParseException {
        final String path1 = getClass().getResource("/type-provider/test.yang").getPath();
        final String path2 = getClass().getResource("/type-provider/ietf-inet-types@2010-09-24.yang").getPath();
        final SchemaContext context = resolveSchemaContextFromFiles(path1, path2);
        assertNotNull(context);
        TypeProviderImpl provider = new TypeProviderImpl(context);
        Module m = context.findModuleByName("test", new SimpleDateFormat("yyyy-MM-dd").parse("2013-10-08"));

        LeafSchemaNode leaf = (LeafSchemaNode)m.getDataChildByName("ext-binary");
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-bits");
        actual = provider.getTypeDefaultConstruction(leaf);
        //assertEquals("new " + PKG + "MyBits(false, false, true)", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-boolean");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBoolean(new java.lang.Boolean(\"true\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-decimal64");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyDecimal64(new java.math.BigDecimal(\"3.14\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-empty");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyEmpty(new java.lang.Boolean(\"false\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-enumeration");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyEnumeration(" + PKG + "MyEnumeration.Seven)", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-8");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "My8(new java.lang.Byte(\"11\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-16");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "My16(new java.lang.Short(\"111\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-32");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "My32(new java.lang.Integer(\"1111\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-64");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "My64(new java.lang.Long(\"11111\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-leafref");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"1.234\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-string");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyString(\"name\")", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-u8");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyU8(new java.lang.Short(\"11\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-u16");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyU16(new java.lang.Integer(\"111\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-u32");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyU32(new java.lang.Long(\"1111\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ext-u64");
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyU64(new java.math.BigInteger(\"11111\"))", actual);

        leaf = (LeafSchemaNode)m.getDataChildByName("ip-leaf");
        actual = provider.getTypeDefaultConstruction(leaf);
        String exp = "new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.Ipv4Address(\"0.0.0.1\")";
        assertEquals(exp, actual);
    }

}
