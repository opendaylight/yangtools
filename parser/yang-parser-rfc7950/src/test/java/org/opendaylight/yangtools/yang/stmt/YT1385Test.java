/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YT1385Test extends AbstractYangTest {
    @Test
    void testSameModuleWrongUnique() {
        assertSourceExceptionMessage("/bugs/YT1385/foo.yang")
            .startsWith("Following components of unique statement argument refer to non-existent nodes: "
                + "[Descendant{qnames=[(foo)bar]}] [at ")
            .endsWith("YT1385/foo.yang:7:5]");
    }
}
