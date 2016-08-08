/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import org.junit.BeforeClass;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public abstract class AbstractUniqueConstraintTest {
    static EffectiveModelContext TEST_MODEL;

    @BeforeClass
    public static void beforeClass() {
        TEST_MODEL = TestModel.createTestContext("/yt570.yang");
    }
}
