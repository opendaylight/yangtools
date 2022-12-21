/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

class YT1370Test {
    @Test
    void testAugmentUnsupportedByFeatures() throws Exception {
        assertNotNull(StmtTestUtils.parseYangSources("/bugs/YT1370", Set.of(), YangParserConfiguration.DEFAULT));
    }
}
