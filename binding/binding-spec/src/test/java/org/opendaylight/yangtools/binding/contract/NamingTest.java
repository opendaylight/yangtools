/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangDataName;

class NamingTest {
    @Test
    void testGetModelRootPackageName() {
        assertEquals("org.opendaylight.yang.gen.v1.test.rev990939",
            Naming.getModelRootPackageName("org.opendaylight.yang.gen.v1.test.rev990939"));
    }

    @Test
    void testGetMethodName() {
        assertEquals("testLocalName", Naming.getMethodName(QName.create("testNS", "testLocalName")));
        assertEquals("testYangIdentifier", Naming.getMethodName("TestYangIdentifier"));
    }

    @Test
    void testGetClassName() {
        assertEquals("TestClass", Naming.getClassName(QName.create("testNS", "testClass")));
        assertEquals("TestClass", Naming.getClassName("testClass"));
        assertEquals("", Naming.getClassName(""));
        assertEquals("SomeTestingClassName", Naming.getClassName("  some-testing_class name   "));
        assertEquals("_0SomeTestingClassName", Naming.getClassName("  0 some-testing_class name   "));
    }

    @Test
    void testGetPropertyName() {
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
            Naming.getRootPackageName(QNameModule.ofRevision(
                "urn:m*o+d,u;l=e.n/a-m@e.t$e#s't.case.1digit", "2013-09-10")));
        assertEquals("_1testpublic", Naming.normalizePackageName("1testpublic"));
        assertEquals("Test", Naming.getGetterSuffix(QName.create("test", "test")));
        assertEquals("XmlClass", Naming.getGetterSuffix(QName.create("test", "class")));
    }

    @Test
    void yangDataMapping() {
        final var ns = QNameModule.of("unused");

        // single ascii compliant non-conflicting word - remain as is
        assertEquals("single", Naming.mapYangDataName(new YangDataName(ns, "single")));
        // ascii compliant - non-compliany chars only encoded
        assertEquals("$abc$20$cde", Naming.mapYangDataName(new YangDataName(ns, "abc cde")));
        // latin1 compliant -> latin chars normalized, non-compliant chars are encoded
        assertEquals("$ľaľaho$20$papľuhu", Naming.mapYangDataName(new YangDataName(ns, "ľaľaho papľuhu")));
        // latin1 non-compliant - all non-compliant characters encoded
        assertEquals("$привет$20$papľuhu", Naming.mapYangDataName(new YangDataName(ns, "привет papľuhu")));
    }
}