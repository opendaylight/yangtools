/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

class Bug7865Test extends AbstractYangTest {
    @Test
    void test() {
        final var context = assertEffectiveModelDir("/bugs/bug7865");
        final var unknownSchemaNodes = assertInstanceOf(ContainerSchemaNode.class,
            context.getDataChildByName(foo("root"))).asEffectiveStatement().getDeclared()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownSchemaNodes.size());

        final var unknownNode = unknownSchemaNodes.iterator().next();
        final var subUnknownSchemaNodes = unknownNode.declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, subUnknownSchemaNodes.size());

        final var subUnknownNode = subUnknownSchemaNodes.iterator().next();
        final var subSubUnknownSchemaNodes = subUnknownNode.declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, subSubUnknownSchemaNodes.size());

        assertEquals("p", subSubUnknownSchemaNodes.iterator().next().argument());
    }

    private static QName foo(final String localName) {
        return QName.create("foo", localName);
    }
}
