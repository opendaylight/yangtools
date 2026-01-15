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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

class Bug7037Test extends AbstractYangTest {
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";

    @Test
    void test() {
        final var context = assertEffectiveModelDir("/bugs/bug7037");

        final var unknownSchemaNodes = context.getModuleStatement(foo("foo")).requireDeclared()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownSchemaNodes.size());

        final var first = unknownSchemaNodes.iterator().next();
        final var firstUnknownNodes = first.declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, firstUnknownNodes.size());

        final var barExtCont = firstUnknownNodes.iterator().next();
        assertEquals(bar("container"), barExtCont.statementDefinition().statementName());
        assertEquals("bar-ext-con", barExtCont.argument());

        final var root = context.getDataChildByName(foo("root"));
        final var rootUnknownNodes = assertInstanceOf(ContainerSchemaNode.class, root).asEffectiveStatement()
            .requireDeclared()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(2, rootUnknownNodes.size());

        final var rootUnknownNodeMap = rootUnknownNodes.stream()
            .collect(Collectors.toMap(u -> u.statementDefinition().statementName(), u -> u));

        final var barExt = rootUnknownNodeMap.get(bar("bar-ext"));
        final var barExtUnknownNodes = barExt.declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(3, barExtUnknownNodes.size());

        UnrecognizedStatement barExtCont2 = null;
        for (var next : barExtUnknownNodes) {
            if (bar("container").equals(next.statementDefinition().statementName())) {
                barExtCont2 = next;
                break;
            }
        }
        assertNotNull(barExtCont2);
        assertEquals("bar-ext-con-2", barExtCont2.argument());

        final var fooExt = rootUnknownNodeMap.get(foo("foo-ext"));
        final var fooUnknownNodes = fooExt.declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, fooUnknownNodes.size());

        final var fooExtCont = fooUnknownNodes.iterator().next();
        assertEquals(foo("container"), fooExtCont.statementDefinition().statementName());
        assertEquals("foo-ext-con", fooExtCont.argument());
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, localName);
    }
}
