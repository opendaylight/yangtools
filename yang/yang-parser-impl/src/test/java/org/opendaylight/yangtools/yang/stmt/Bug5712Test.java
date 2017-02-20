/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public class Bug5712Test {

    @Test
    public void testTypedefWithNewStatementParser() throws Exception {
        SchemaContext schemaContext = StmtTestUtils.parseYangSources("/bugs/bug5712");
        assertNotNull(schemaContext);

        Module badModule = schemaContext.findModuleByName("bad", null);
        assertNotNull(badModule);

        checkThing2TypeDef(badModule);
    }

    private static void checkThing2TypeDef(final Module badModule) {
        TypeDefinition<?> thing2 = null;
        for (TypeDefinition<?> typeDef : badModule.getTypeDefinitions()) {
            if (typeDef.getQName().getLocalName().equals("thing2")) {
                thing2 = typeDef;
                break;
            }
        }

        assertNotNull(thing2);
        TypeDefinition<?> baseType = thing2.getBaseType();
        assertEquals(QName.create("urn:opendaylight:bad", "2016-04-11", "thing"), baseType.getQName());
    }
}
