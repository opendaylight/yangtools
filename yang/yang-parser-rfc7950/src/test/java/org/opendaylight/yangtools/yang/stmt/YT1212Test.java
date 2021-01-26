/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

public class YT1212Test {
    @Test
    public void testActiontatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1212/anyxml.yang")
            .getModuleStatements()
            .get(QNameModule.create(URI.create("foo")));
        assertNotNull(module);

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
    public void testLeafStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1212/leaf.yang")
            .getModuleStatements()
            .get(QNameModule.create(URI.create("foo")));
        assertNotNull(module);

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
    public void testContainerStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1212/container.yang")
            .getModuleStatements()
            .get(QNameModule.create(URI.create("foo")));
        assertNotNull(module);

        final NotificationEffectiveStatement notif =
            module.findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow();
        final List<GroupingEffectiveStatement> groupings = notif.effectiveSubstatements().stream()
            .filter(GroupingEffectiveStatement.class::isInstance).map(GroupingEffectiveStatement.class::cast)
            .collect(Collectors.toList());
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
