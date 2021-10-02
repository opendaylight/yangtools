/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Test;

public class Bug8831Test extends AbstractYangTest {
    @Test
    public void test() throws Exception {
        assertEffectiveModelDir("/bugs/bug8831/valid");
    }

    @Test
    public void invalidModelsTest() {
        assertSourceException(containsString("has default value 'any' marked with an if-feature statement"),
            "/bugs/bug8831/invalid/inv-model.yang");
    }

    @Test
    public void invalidModelsTest2() {
        assertSourceException(containsString("has default value 'any' marked with an if-feature statement"),
            "/bugs/bug8831/invalid/inv-model2.yang");
    }
}
