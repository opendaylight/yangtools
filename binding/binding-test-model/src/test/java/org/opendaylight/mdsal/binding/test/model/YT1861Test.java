/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.yt1861.two.norev.One1;
import org.opendaylight.yang.gen.v1.yt1861.two.norev.One1Builder;

class YT1861Test {
    private static final One1 FIRST = new One1Builder().build();
    private static final One1 SECOND = new One1Builder().build();

    @Test
    void emptyAugmentationsSameHashCode() {
        assertEquals(FIRST.hashCode(), SECOND.hashCode());
   }

    @Test
    void emptyAugmentationsAreEqual() {
        assertEquals(FIRST, SECOND);
    }
}
