/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Bug3859Test {
    @Test
    public void test() throws Exception {
        assertEquals(1, TestUtils.loadModules("/bugs/bug3859").findModules("reference-in-unknown").size());
    }
}
