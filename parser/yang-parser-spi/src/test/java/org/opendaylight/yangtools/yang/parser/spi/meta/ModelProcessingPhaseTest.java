/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ModelProcessingPhaseTest {
    @Test
    void testSequencing() {
        assertNull(ModelProcessingPhase.INIT.getPreviousPhase());
        assertEquals(ModelProcessingPhase.INIT, ModelProcessingPhase.SOURCE_PRE_LINKAGE.getPreviousPhase());
        assertEquals(ModelProcessingPhase.SOURCE_PRE_LINKAGE, ModelProcessingPhase.SOURCE_LINKAGE.getPreviousPhase());
        assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, ModelProcessingPhase.STATEMENT_DEFINITION.getPreviousPhase());
        assertEquals(ModelProcessingPhase.STATEMENT_DEFINITION,
            ModelProcessingPhase.FULL_DECLARATION.getPreviousPhase());
        assertEquals(ModelProcessingPhase.FULL_DECLARATION,
            ModelProcessingPhase.EFFECTIVE_MODEL.getPreviousPhase());
    }
}
