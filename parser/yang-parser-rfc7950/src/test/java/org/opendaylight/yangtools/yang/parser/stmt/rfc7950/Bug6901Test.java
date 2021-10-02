/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.Test;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

public class Bug6901Test extends AbstractYangTest {
    @Test
    public void ifFeature11EnumBitTest() throws Exception {
        assertEffectiveModel("/rfc7950/bug6901/foo.yang");
    }

    @Test
    public void ifFeatureOnDefaultValueEnumTest() {
        assertSourceException(
            startsWith("Leaf '(foo)enum-leaf' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-enum.yang");
    }

    @Test
    public void ifFeatureOnDefaultValueEnumTest2() {
        assertSourceException(
            startsWith("Leaf '(foo)enum-leaf' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-enum-2.yang");
    }

    @Test
    public void ifFeatureOnDefaultValueEnumTest3() {
        assertSourceException(startsWith(
            "Leaf '(foo)enum-leaf' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-enum-3.yang");
    }

    @Test
    public void ifFeatureOnDefaultValueBitTest() {
        assertSourceException(
            startsWith("Typedef '(foo)bits-typedef-2' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-bit.yang");
    }

    @Test
    public void ifFeatureOnDefaultValueUnionTest() {
        assertSourceException(
            startsWith("Leaf '(foo)union-leaf' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-union.yang");
    }

    @Test
    public void unsupportedFeatureTest() {
        assertSourceException(containsString("has default value 'two' marked with an if-feature statement"),
            "/rfc7950/bug6901/invalid-foo-enum.yang");
    }

    @Test
    public void ifFeature10EnumTest() {
        assertInvalidSubstatementException(startsWith("IF_FEATURE is not valid for ENUM"),
            "/rfc7950/bug6901/invalid-foo-10-enum.yang");
    }

    @Test
    public void ifFeature10BitTest() {
        assertInvalidSubstatementException(startsWith("IF_FEATURE is not valid for BIT"),
            "/rfc7950/bug6901/invalid-foo-10-bit.yang");
    }
}
