/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.contract;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public class NamingTest {

    @Test
    public void testGetModelRootPackageName() {
        assertEquals("org.opendaylight.yang.gen.v1.test.rev990939",
            Naming.getModelRootPackageName("org.opendaylight.yang.gen.v1.test.rev990939"));
    }

    @Test
    public void testGetMethodName() {
        assertEquals("testLocalName", Naming.getMethodName(QName.create("testNS", "testLocalName")));
        assertEquals("testYangIdentifier", Naming.getMethodName("TestYangIdentifier"));
    }

    @Test
    public void testGetClassName() {
        assertEquals("TestClass", Naming.getClassName(QName.create("testNS", "testClass")));
        assertEquals("TestClass", Naming.getClassName("testClass"));
        assertEquals("", Naming.getClassName(""));
        assertEquals("SomeTestingClassName", Naming.getClassName("  some-testing_class name   "));
        assertEquals("_0SomeTestingClassName", Naming.getClassName("  0 some-testing_class name   "));
    }

    @Test
    public void testGetPropertyName() {
        assertEquals("test", Naming.getPropertyName("Test"));
        assertEquals("test", Naming.getPropertyName("test"));
        assertEquals("xmlClass", Naming.getPropertyName("Class"));
        assertEquals("_5", Naming.getPropertyName("5"));
        assertEquals("", Naming.getPropertyName(""));
        assertEquals("someTestingParameterName", Naming.getPropertyName("  some-testing_parameter   name   "));
        assertEquals("_0someTestingParameterName", Naming.getPropertyName("  0some-testing_parameter   name   "));
    }

    @Test
    public void basicTest() {
        assertEquals("org.opendaylight.yang.gen.v1.test.uri.rev171026",
            Naming.getRootPackageName(QName.create("test:URI", "2017-10-26", "test")));
        assertEquals("org.opendaylight.yang.gen.v1.urn.m.o.d.u.l.e.n.a.m.e.t.e.s.t._case._1digit.rev130910",
            Naming.getRootPackageName(QNameModule.create(
                XMLNamespace.of("urn:m*o+d,u;l=e.n/a-m@e.t$e#s't.case.1digit"), Revision.of("2013-09-10"))));
        assertEquals("_1testpublic", Naming.normalizePackageName("1testpublic"));
        assertEquals("Test", Naming.getGetterSuffix(QName.create("test", "test")));
        assertEquals("XmlClass", Naming.getGetterSuffix(QName.create("test", "class")));
    }

    @Test
    public void mapEnumAssignedNamesTest() {
        // Okay identifier
        assertEqualMapping("__", "__");
        assertEqualMapping("True", "true");
        assertEqualMapping("ĽaľahoPapľuhu", "ľaľaho papľuhu");

        // Contains a '$', but that's okay
        assertEqualMapping("$", "$");
        assertEqualMapping("$abc", "$abc");
        assertEqualMapping("A$bc", "a$bc");
        assertEqualMapping("Ab$c", "ab$c");
        assertEqualMapping("Abc$", "abc$");

        // Mostly okay, but numbers need to be prefixed
        assertEqualMapping(List.of("AZ", "_09"), List.of("a-z", "0-9"));

        // Invalid identifier (conflicts with a Java 9 keyword)
        assertEqualMapping("$_", "_");

        // Invalid characters, fall back to bijection
        assertEqualMapping("$$2A$", "*");
        assertEqualMapping("$$2E$", ".");
        assertEqualMapping("$$2F$", "/");
        assertEqualMapping("$$3F$", "?");
        assertEqualMapping("$a$2A$a", "a*a");

        // Conflict, fallback to bijection
        assertEqualMapping(List.of("_09", "$0$2D$9"), List.of("_09", "0-9"));
        assertEqualMapping(List.of("$09", "$0$2D$9"), List.of("09", "0-9"));
        assertEqualMapping(List.of("aZ", "$a$2D$z"), List.of("aZ", "a-z"));
        assertEqualMapping(List.of("$a2$2E$5", "a25"), List.of("a2.5", "a25"));
        assertEqualMapping(List.of("$a2$2E$5", "$a2$2D$5"), List.of("a2.5", "a2-5"));
        assertEqualMapping(List.of("$ľaľaho$20$papľuhu", "$ľaľaho$20$$20$papľuhu"),
            List.of("ľaľaho papľuhu", "ľaľaho  papľuhu"));
    }

    private static void assertEqualMapping(final String mapped, final String yang) {
        assertEqualMapping(List.of(mapped), List.of(yang));
    }

    private static void assertEqualMapping(final List<String> mapped, final List<String> yang) {
        assertEquals(mapped.size(), yang.size());
        final Map<String, String> expected = new HashMap<>();
        for (int i = 0; i < mapped.size(); ++i) {
            expected.put(yang.get(i), mapped.get(i));
        }

        assertEquals(expected, Naming.mapEnumAssignedNames(yang));
    }

    @Test
    public void yangDataMapping() {
        // single ascii compliant non-conflicting word - remain as is
        assertEquals("single", Naming.mapYangDataName("single"));
        // ascii compliant - non-compliany chars only encoded
        assertEquals("$abc$20$cde", Naming.mapYangDataName("abc cde"));
        // latin1 compliant -> latin chars normalized, non-compliant chars are encoded
        assertEquals("$ľaľaho$20$papľuhu", Naming.mapYangDataName("ľaľaho papľuhu"));
        // latin1 non-compliant - all non-compliant characters encoded
        assertEquals("$привет$20$papľuhu", Naming.mapYangDataName("привет papľuhu"));
    }
}