/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Test case for bug YANGTOOLS-1261.
 * https://jira.opendaylight.org/browse/YANGTOOLS-1261
 */
public class Bug1261Test {

    @Test
    public void test() throws Exception {
        SchemaContext context = TestUtils.parseYangSources("/bugs/bug1261");
        assertNotNull(context);
    }

}
