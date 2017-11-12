/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;

public class Bug3859Test {
    @Test
    public void test() throws Exception {
        final Module bug3859 = TestUtils.findModule(
            TestUtils.loadModules(getClass().getResource("/bugs/bug3859").toURI()), "reference-in-unknown").get();
        assertNotNull(bug3859);
    }
}
