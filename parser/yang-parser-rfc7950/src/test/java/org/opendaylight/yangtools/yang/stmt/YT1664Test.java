/*
 * Copyright (c) 2025 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YT1664Test extends AbstractYangTest {
    @Test
    void testLeafListBadElements() {
        assertSourceExceptionMessage("/bugs/YT1664/bar.yang")
            .startsWith("Conflicting 'min-elements 2' and 'max-elements 1' [at ");
    }

    @Test
    void testListBadElements() {
        assertSourceExceptionMessage("/bugs/YT1664/foo.yang")
            .startsWith("Conflicting 'min-elements 2' and 'max-elements 1' [at ");
    }
}
