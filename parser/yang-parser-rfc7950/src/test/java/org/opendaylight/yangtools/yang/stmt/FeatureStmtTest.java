/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class FeatureStmtTest extends AbstractModelTest {

    // FIXME: add model-based assertions

    @Test
    void testDuplicateFeature() {
        assertSourceExceptionMessage("/duplicate-feature.yang")
            .startsWith("Feature name collision: '(foo)bar' already declared at ")
            .containsOnlyOnce(":5:3 [at ")
            .endsWith(":7:3]");
    }
}
