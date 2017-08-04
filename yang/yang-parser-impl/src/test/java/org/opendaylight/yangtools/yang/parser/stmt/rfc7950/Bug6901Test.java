/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6901Test {

    @Test
    public void ifFeature11EnumBitTest() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6901/foo.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void ifFeatureOnDefaultValueEnumTest() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6901/invalid-foo-enum.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Leaf '(foo?revision=1970-01-01)enum-leaf' has default value 'two' marked with an if-feature statement."));
        }
    }

    @Test
    public void ifFeatureOnDefaultValueEnumTest2() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6901/invalid-foo-enum-2.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Leaf '(foo?revision=1970-01-01)enum-leaf' has default value 'two' marked with an if-feature statement."));
        }
    }

    @Test
    public void ifFeatureOnDefaultValueEnumTest3() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6901/invalid-foo-enum-3.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Leaf '(foo?revision=1970-01-01)enum-leaf' has default value 'two' marked with an if-feature statement."));
        }
    }

    @Test
    public void ifFeatureOnDefaultValueBitTest() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6901/invalid-foo-bit.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Typedef '(foo?revision=1970-01-01)bits-typedef-2' has default value 'two' marked with an if-feature statement."));
        }
    }

    @Test
    public void ifFeatureOnDefaultValueUnionTest() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6901/invalid-foo-union.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Leaf '(foo?revision=1970-01-01)union-leaf' has default value 'two' marked with an if-feature statement."));
        }
    }

    @Test
    public void unsupportedFeatureTest() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6901/invalid-foo-enum.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(
                    e.getCause().getMessage().contains("has default value 'two' marked with an if-feature statement"));
        }
    }

    @Test
    public void ifFeature10EnumTest() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6901/invalid-foo-10-enum.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith("IF_FEATURE is not valid for ENUM"));
        }
    }

    @Test
    public void ifFeature10BitTest() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6901/invalid-foo-10-bit.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith("IF_FEATURE is not valid for BIT"));
        }
    }
}