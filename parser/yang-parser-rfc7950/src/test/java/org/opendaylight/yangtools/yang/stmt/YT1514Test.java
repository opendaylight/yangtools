/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import java.util.Set;
import org.junit.jupiter.api.Test;

class YT1514Test extends AbstractYangTest {
    @Test
    void testLeafFooFeatureSupported() {
        assertEffectiveModelDir("/bugs/YT1514", Set.of());
    }
}
