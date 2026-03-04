/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6901Test extends AbstractYangTest {
    @Test
    void ifFeature11EnumBitTest() {
        assertEffectiveModel("/rfc7950/bug6901/foo.yang");
    }

    @Test
    void ifFeatureOnDefaultValueEnumTest() {
        assertSourceExceptionMessage("/rfc7950/bug6901/invalid-foo-enum.yang")
            .startsWith("Leaf '(foo)enum-leaf' has default value 'two' marked with an if-feature statement.");
    }

    @Test
    void ifFeatureOnDefaultValueEnumTest2() {
        assertSourceExceptionMessage("/rfc7950/bug6901/invalid-foo-enum-2.yang")
            .startsWith("Leaf '(foo)enum-leaf' has default value 'two' marked with an if-feature statement.");
    }

    @Test
    void ifFeatureOnDefaultValueEnumTest3() {
        assertSourceExceptionMessage("/rfc7950/bug6901/invalid-foo-enum-3.yang")
            .startsWith("Leaf '(foo)enum-leaf' has default value 'two' marked with an if-feature statement.");
    }

    @Test
    void ifFeatureOnDefaultValueBitTest() {
        assertSourceExceptionMessage("/rfc7950/bug6901/invalid-foo-bit.yang")
            .startsWith("Typedef '(foo)bits-typedef-2' has default value 'two' marked with an if-feature statement.");
    }

    @Test
    void ifFeatureOnDefaultValueUnionTest() {
        assertSourceExceptionMessage("/rfc7950/bug6901/invalid-foo-union.yang")
            .startsWith("Leaf '(foo)union-leaf' has default value 'two' marked with an if-feature statement.");
    }

    @Test
    void unsupportedFeatureTest() {
        assertSourceExceptionMessage("/rfc7950/bug6901/invalid-foo-enum.yang")
            .contains("has default value 'two' marked with an if-feature statement");
    }

    @Test
    void ifFeature10EnumTest() {
        assertThat(assertInvalidSubstatementException("/rfc7950/bug6901/invalid-foo-10-enum.yang").getMessage())
            .startsWith("statement enum does not allow if-feature substatements: 1 present [at ");
    }

    @Test
    void ifFeature10BitTest() {
        assertThat(assertInvalidSubstatementException("/rfc7950/bug6901/invalid-foo-10-bit.yang").getMessage())
            .startsWith("statement bit does not allow if-feature substatements: 1 present [at ");
    }
}
