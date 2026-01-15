/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

/**
 * Test ANTLR4 grammar capability to parse description statement in unknown node.
 *
 * <p>Note: Everything under unknown node is unknown node.
 */
class Bug1412Test extends AbstractYangTest {
    @Test
    void test() {
        final Module bug1412 = assertEffectiveModelDir("/bugs/bug1412").findModules("bug1412").iterator().next();

        final ContainerSchemaNode node = (ContainerSchemaNode) bug1412.getDataChildByName(QName.create(
            bug1412.getQNameModule(), "node"));
        var unknownNodes = node.asEffectiveStatement().requireDeclared()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownNodes.size());
        final UnrecognizedStatement action = unknownNodes.iterator().next();

        assertEquals(QName.create("urn:test:bug1412:ext:definitions", "2014-07-25", "action"),
            action.statementDefinition().statementName());
        assertEquals("hello", action.argument());

        unknownNodes = action.declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(4, unknownNodes.size());
        UnrecognizedStatement info = null;
        UnrecognizedStatement description = null;
        UnrecognizedStatement actionPoint = null;
        UnrecognizedStatement output = null;
        for (final UnrecognizedStatement un : unknownNodes) {
            final String name = un.statementDefinition().statementName().getLocalName();
            switch (name) {
                case "info":
                    info = un;
                    break;
                case "description":
                    description = un;
                    break;
                case "actionpoint":
                    actionPoint = un;
                    break;
                case "output":
                    output = un;
                    break;
                case null:
                default:
                    break;
            }
        }

        assertNotNull(info);
        assertEquals(QName.create("urn:test:bug1412:ext:definitions", "2014-07-25", "info"),
            info.statementDefinition().statementName());
        assertEquals("greeting", info.argument());

        assertNotNull(description);
        assertEquals(QName.create("urn:test:bug1412:ext:definitions", "2014-07-25", "description"),
            description.statementDefinition().statementName());
        assertEquals("say greeting", description.argument());

        assertNotNull(actionPoint);
        assertEquals(QName.create("urn:test:bug1412:ext:definitions", "2014-07-25", "actionpoint"),
            actionPoint.statementDefinition().statementName());
        assertEquals("entry", actionPoint.argument());

        assertNotNull(output);
        assertEquals(QName.create("urn:test:bug1412:ext:definitions", "2014-07-25", "output"),
            output.statementDefinition().statementName());
        assertEquals(Empty.value(), output.argument());
    }
}
