/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

/**
 * Test antlr grammar capability to parse nested unknown nodes.
 */
class Bug394Test extends AbstractYangTest {
    @Test
    void testParseList() {
        final var context = assertEffectiveModelDir("/bugs/bug394-retest");
        final var bug394 = context.findModules("bug394").iterator().next();
        final var bug394_ext = context.findModules("bug394-ext").iterator().next();

        final var logrecords = assertInstanceOf(ContainerSchemaNode.class,
            bug394.dataChildByName(QName.create(bug394.getQNameModule(), "logrecords")));
        assertNotNull(logrecords);

        final var nodes = logrecords.asEffectiveStatement().requireDeclared()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(2, nodes.size());

        final var extensions = bug394_ext.getExtensionSchemaNodes().stream()
            .map(ExtensionDefinition::getQName)
            .collect(Collectors.toUnmodifiableSet());
        assertEquals(3, extensions.size());

        final var it = nodes.iterator();
        assertTrue(extensions.contains(it.next().statementDefinition().statementName()));
        assertTrue(extensions.contains(it.next().statementDefinition().statementName()));
    }
}
