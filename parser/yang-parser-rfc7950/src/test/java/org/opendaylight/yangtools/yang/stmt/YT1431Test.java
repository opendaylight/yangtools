/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import java.util.Set;
import org.junit.Test;

public class YT1431Test {
    @Test
    public void testUnsupportedChoiceLeaf() throws Exception {
        final var context = StmtTestUtils.parseYangSource("/bugs/YT1431/foo.yang", Set.of());
        assertNotNull(context);
    }

    @Test
    public void testUnsupportedChoiceLeafAugment() throws Exception {
        final var context = StmtTestUtils.parseYangSource("/bugs/YT1431/bar.yang", Set.of());
        assertNotNull(context);
    }
}
