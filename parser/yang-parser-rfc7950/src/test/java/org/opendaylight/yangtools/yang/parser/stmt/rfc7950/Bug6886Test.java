/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.Test;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

public class Bug6886Test extends AbstractYangTest {
    @Test
    public void yang11UnquotedStrTest() {
        assertSourceException(startsWith("YANG 1.1: unquoted string (illegalchars\"test1) contains illegal characters"),
            "/rfc7950/bug6886/yang11/foo.yang");
    }

    @Test
    public void yang11UnquotedStrTest2() throws Exception {
        assertSourceException(startsWith("YANG 1.1: unquoted string (illegalchars'test2) contains illegal characters"),
            "/rfc7950/bug6886/yang11/foo2.yang");
    }

    @Test
    public void yang11DoubleQuotedStrTest() throws Exception {
        assertSourceException(startsWith("YANG 1.1: illegal double quoted string "
            + "(i\\\\\\\\l\\nl\\te\\\"\\galcharstest1). In double quoted string the backslash must be followed "
            + "by one of the following character [n,t,\",\\], but was 'g'."),
            "/rfc7950/bug6886/yang11/foo3.yang");
    }

    @Test
    public void yang10UnquotedStrTest() throws Exception {
        assertEffectiveModel("/rfc7950/bug6886/yang10/foo.yang");
    }

    @Test
    public void yang10UnquotedStrTest2() throws Exception {
        assertEffectiveModel("/rfc7950/bug6886/yang10/foo2.yang");
    }

    @Test
    public void yang10DoubleQuotedStrTest() throws Exception {
        assertEffectiveModel("/rfc7950/bug6886/yang10/foo3.yang");
    }
}
