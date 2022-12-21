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

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6901Test extends AbstractYangTest {
    @Test
    void ifFeature11EnumBitTest() {
        assertEffectiveModel("/rfc7950/bug6901/foo.yang");
    }

    @Test
    void ifFeatureOnDefaultValueEnumTest() {
        assertSourceException(
            startsWith("Leaf '(foo)enum-leaf' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-enum.yang");
    }

    @Test
    void ifFeatureOnDefaultValueEnumTest2() {
        assertSourceException(
            startsWith("Leaf '(foo)enum-leaf' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-enum-2.yang");
    }

    @Test
    void ifFeatureOnDefaultValueEnumTest3() {
        assertSourceException(startsWith(
            "Leaf '(foo)enum-leaf' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-enum-3.yang");
    }

    @Test
    void ifFeatureOnDefaultValueBitTest() {
        assertSourceException(
            startsWith("Typedef '(foo)bits-typedef-2' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-bit.yang");
    }

    @Test
    void ifFeatureOnDefaultValueUnionTest() {
        assertSourceException(
            startsWith("Leaf '(foo)union-leaf' has default value 'two' marked with an if-feature statement."),
            "/rfc7950/bug6901/invalid-foo-union.yang");
    }

    @Test
    void unsupportedFeatureTest() {
        assertSourceException(containsString("has default value 'two' marked with an if-feature statement"),
            "/rfc7950/bug6901/invalid-foo-enum.yang");
    }

    @Test
    void ifFeature10EnumTest() {
        assertInvalidSubstatementException(startsWith("IF_FEATURE is not valid for ENUM"),
            "/rfc7950/bug6901/invalid-foo-10-enum.yang");
    }

    @Test
    void ifFeature10BitTest() {
        assertInvalidSubstatementException(startsWith("IF_FEATURE is not valid for BIT"),
            "/rfc7950/bug6901/invalid-foo-10-bit.yang");
    }
}
