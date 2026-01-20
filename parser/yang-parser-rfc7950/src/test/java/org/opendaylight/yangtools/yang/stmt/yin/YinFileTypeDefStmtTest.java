/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class YinFileTypeDefStmtTest extends AbstractYinModulesTest {
    @Test
    void testTypedef() {
        final var testModule = context.findModules("config").iterator().next();
        assertNotNull(testModule);

        final var typeDefs = testModule.getTypeDefinitions();
        assertEquals(1, typeDefs.size());

        final var typeDefIterator = typeDefs.iterator();
        final var typeDef = typeDefIterator.next();
        assertNotNull(typeDef);
        assertEquals("service-type-ref", typeDef.getQName().getLocalName());
        assertEquals(Optional.of("Internal type of references to service type identity."), typeDef.getDescription());
        final var baseType = typeDef.getBaseType();
        assertNotNull(baseType);
        assertEquals("identityref", baseType.getQName().getLocalName());
    }
}
