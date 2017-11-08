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
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class TypedefSubStmtsTest {

    @Test
    public void typedefSubStmtsTest() throws ReactorException {
        SchemaContext result = DefaultReactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/typedef-substmts-test/typedef-substmts-test.yang"))
                .buildEffective();
        assertNotNull(result);

        Set<TypeDefinition<?>> typedefs = result.getTypeDefinitions();
        assertEquals(1, typedefs.size());

        TypeDefinition<?> typedef = typedefs.iterator().next();
        assertEquals("time-of-the-day", typedef.getQName().getLocalName());
        assertEquals("string", typedef.getBaseType().getQName().getLocalName());
        assertEquals(Optional.of("24-hour-clock"), typedef.getUnits());
        assertEquals("1am", typedef.getDefaultValue().map(Object::toString).orElse(null));
    }
}
