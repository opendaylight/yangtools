/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public abstract class AbstractTestModelTest {
    static SchemaContext SCHEMA_CONTEXT;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext();
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }
}
