/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug9005Test {
    @Test
    public void fooTest() throws Exception {
        for (int i = 0; i < 1; i++) {
            final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug9005/foo-test");
            assertNotNull(context);
        }
    }

    @Test
    public void fooTest2() throws Exception {
        for (int i = 0; i < 1; i++) {
            final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug9005/foo-test-2");
            assertNotNull(context);
        }
    }

    @Test
    public void openconfigTest() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug9005/openconfig");
        assertNotNull(context);
    }
}
