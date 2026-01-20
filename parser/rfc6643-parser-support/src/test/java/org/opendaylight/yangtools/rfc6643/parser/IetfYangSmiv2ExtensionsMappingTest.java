/*
 * Copyright (c) 2015, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc6643.model.api.AliasEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.AliasStatement;

class IetfYangSmiv2ExtensionsMappingTest {
    @Test
    void testGetEffectiveRepresentationClass() {
        assertEquals(AliasEffectiveStatement.class,
            AliasStatement.DEFINITION.getEffectiveRepresentationClass());
    }

    @Test
    void testIsArgumentYinElement() {
        assertFalse(AliasStatement.DEFINITION.getArgumentDefinition().isYinElement());
    }
}
