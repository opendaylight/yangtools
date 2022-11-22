/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

public class YT1465Test extends AbstractYangTest {
    @Test
    public void unsupportedLeafInChoiceAugment() throws Exception {
        assertNotNull(StmtTestUtils.parseYangSource("/bugs/YT1465/foo.yang", YangParserConfiguration.DEFAULT,
            Set.of()));
    }
}
