/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

public class YinFileTypeDefStmtTest extends AbstractYinModulesTest {

    @Test
    public void testTypedef() {
        Module testModule = TestUtils.findModule(context, "config").get();
        assertNotNull(testModule);

        Set<TypeDefinition<?>> typeDefs = testModule.getTypeDefinitions();
        assertEquals(1, typeDefs.size());

        Iterator<TypeDefinition<?>> typeDefIterator = typeDefs.iterator();
        TypeDefinition<?> typeDef = typeDefIterator.next();
        assertEquals("service-type-ref", typeDef.getQName().getLocalName());
        assertEquals(Optional.of("Internal type of references to service type identity."), typeDef.getDescription());
        assertEquals("identityref", typeDef.getBaseType().getQName().getLocalName());
    }
}
