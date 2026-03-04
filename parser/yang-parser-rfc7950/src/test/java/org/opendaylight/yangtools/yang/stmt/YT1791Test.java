/*
 * Copyright (c) 2025 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YT1791Test extends AbstractYangTest {
    @Test
    void testOverlappingTwo() {
        assertSourceExceptionMessage("/bugs/YT1791/two.yang")
            .startsWith("Some of the value ranges in 120..150|140 are not disjoint [at ")
            .endsWith("/two.yang:8:7]");
    }
}
