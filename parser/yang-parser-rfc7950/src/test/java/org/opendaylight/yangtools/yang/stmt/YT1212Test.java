/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

class YT1212Test extends AbstractYangTest {
    @Test
    void testActiontatementReuse() {
        final var module = assertEffectiveModel("/bugs/YT1212/anyxml.yang").getModuleStatement(QNameModule.of("foo"));

        final AnyxmlEffectiveStatement grpFoo = module
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(AnyxmlEffectiveStatement.class).orElseThrow();
        final AnyxmlEffectiveStatement foo = module
            .findFirstEffectiveSubstatement(AnyxmlEffectiveStatement.class).orElseThrow();

        // The statements should not be the same due SchemaPath being part of ActionDefinition
        assertNotSame(foo, grpFoo);
        // The statements are instantiated in the same module, hence they should have the same argument
        assertSame(foo.argument(), grpFoo.argument());
        // All substatements are context-independent, hence they get reused
        assertSame(foo.effectiveSubstatements(), grpFoo.effectiveSubstatements());
    }

    @Test
    void testLeafStatementReuse() {
        final var module = assertEffectiveModel("/bugs/YT1212/leaf.yang").getModuleStatement(QNameModule.of("foo"));

        final LeafEffectiveStatement grpFoo = module
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();
        final LeafEffectiveStatement foo = module
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();

        // The statements should not be the same due SchemaPath being part of LeafSchemaNode
        assertNotSame(foo, grpFoo);
        // The statements are instantiated in the same module, hence they should have the same argument
        assertSame(foo.argument(), grpFoo.argument());
        // The 'type' is not context-independent, but it being copy-insensitive and statements get reused
        assertSame(foo.effectiveSubstatements(), grpFoo.effectiveSubstatements());
    }

    @Test
    void testContainerStatementReuse() {
        final var module = assertEffectiveModel("/bugs/YT1212/container.yang")
            .getModuleStatement(QNameModule.of("foo"));

        final var notif = module.findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow();
        final var groupings = notif.effectiveSubstatements().stream()
            .filter(GroupingEffectiveStatement.class::isInstance)
            .map(GroupingEffectiveStatement.class::cast)
            .toList();
        assertEquals(2, groupings.size());
        final GroupingEffectiveStatement grp = groupings.get(0);
        assertEquals("grp", grp.argument().getLocalName());
        final GroupingEffectiveStatement barGrp = groupings.get(1);
        assertEquals("bar", barGrp.argument().getLocalName());
        final ContainerEffectiveStatement bar = notif.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class)
            .orElseThrow();

        // Container needs to be reused
        assertSame(bar.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow(),
            barGrp.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow());
    }
}
