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

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

class YinFileTypeDefStmtTest extends AbstractYinModulesTest {
    @Test
    void testTypedef() {
        Module testModule = context.findModules("config").iterator().next();
        assertNotNull(testModule);

        Collection<? extends TypeDefinition<?>> typeDefs = testModule.getTypeDefinitions();
        assertEquals(1, typeDefs.size());

        Iterator<? extends TypeDefinition<?>> typeDefIterator = typeDefs.iterator();
        TypeDefinition<?> typeDef = typeDefIterator.next();
        assertEquals("service-type-ref", typeDef.getQName().getLocalName());
        assertEquals(Optional.of("Internal type of references to service type identity."), typeDef.getDescription());
        assertEquals("identityref", typeDef.getBaseType().getQName().getLocalName());
    }
}
