/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class Bug5693Test {
    @Test
    void bug5693Test() throws Exception {
        final var features = StmtTestUtils.parseYinSources("/bugs/bug5693").getModules().iterator().next()
            .getFeatures();
        assertNotNull(features);
        assertEquals(1, features.size(), "Module should has exactly one feature");
        assertEquals("test-input-stream-not-closed", features.iterator().next().getQName().getLocalName(),
            "Present feature should has expected local name");
    }
}
