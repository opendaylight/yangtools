/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

class Bug7038Test extends AbstractYangTest {
    @Test
    void unknownNodeTest() {
        final var bar = assertEffectiveModelDir("/bugs/bug7038").getModuleStatement(QNameModule.of("bar"))
            .getDeclared();
        final var decimal64 = bar.findFirstDeclaredSubstatement(UnrecognizedStatement.class).orElseThrow();
        assertEquals("decimal64", decimal64.argument());
        assertEquals(QName.create("foo", "decimal64"), decimal64.statementDefinition().getStatementName());
    }

    @Test
    void testYang11() {
        final var root = (ContainerSchemaNode) assertEffectiveModelDir("/bugs/bug7038/yang11")
            .getDataChildByName(QName.create("foo", "root"));
        final var typedef = ((LeafSchemaNode) root.getDataChildByName(QName.create("foo", "my-leafref")))
            .getType();
        assertFalse(assertInstanceOf(LeafrefTypeDefinition.class, typedef).requireInstance());
    }

    @Test
    void testYang10() {
        assertInvalidSubstatementExceptionDir("/bugs/bug7038/yang10",
            startsWith("REQUIRE_INSTANCE is not valid for TYPE"));
    }
}
