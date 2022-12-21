/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

class Bug4610Test extends AbstractYangTest {
    @Test
    void test() {
        final var context = assertEffectiveModelDir("/bugs/bug4610");
        final Revision revision = Revision.of("2015-12-12");

        // Original
        final QName c1Bar = QName.create(QNameModule.create(XMLNamespace.of("bar"), revision), "c1");
        final ContainerEffectiveStatement g1container = findContainer(context, QName.create(c1Bar, "g1"), c1Bar);
        final QName g1argument = g1container.argument();
        final ContainerStatement g1original = g1container.getDeclared();

        final ContainerEffectiveStatement g2container = findContainer(context, QName.create(c1Bar, "g2"), c1Bar);
        assertEquals(g1argument, g2container.argument());
        assertSame(g1original, g2container.getDeclared());

        final QName c1Foo = QName.create(QNameModule.create(XMLNamespace.of("foo"), revision), "c1");
        final ContainerEffectiveStatement g3container = findContainer(context, QName.create(c1Foo, "g3"), c1Foo);
        assertNotEquals(g1argument, g3container.argument());
        assertSame(g1original, g3container.getDeclared());

        final SchemaTreeEffectiveStatement<?> rootContainer = context.getModuleStatement(c1Foo.getModule())
            .findSchemaTreeNode(QName.create(c1Foo, "root"), c1Foo).orElseThrow();
        assertThat(rootContainer, instanceOf(ContainerEffectiveStatement.class));
        assertNotEquals(g1argument, rootContainer.argument());
        assertSame(g1original, rootContainer.getDeclared());
    }

    private static ContainerEffectiveStatement findContainer(final EffectiveModelContext context, final QName grouping,
        final QName container) {
        final ModuleEffectiveStatement module = context.getModuleStatement(grouping.getModule());
        final GroupingEffectiveStatement grp = module.streamEffectiveSubstatements(GroupingEffectiveStatement.class)
            .filter(stmt -> grouping.equals(stmt.argument()))
            .findAny().orElseThrow();

        final SchemaTreeEffectiveStatement<?> node = grp.findSchemaTreeNode(container).orElse(null);
        assertThat(node, instanceOf(ContainerEffectiveStatement.class));
        return (ContainerEffectiveStatement) node;
    }
}
