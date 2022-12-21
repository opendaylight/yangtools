/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

public abstract class AbstractYinModulesTest {

    static EffectiveModelContext context;

    @BeforeAll
    static void beforeClass() throws Exception {
        context = TestUtils.loadYinModules(
            AbstractYinModulesTest.class.getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, context.getModules().size());
    }

    @AfterAll
    static void afterClass() {
        context = null;
    }
}
