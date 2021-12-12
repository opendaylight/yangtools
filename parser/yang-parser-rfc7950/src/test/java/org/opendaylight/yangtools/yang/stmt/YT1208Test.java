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
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

public class YT1208Test {
    @Test
    public void testAugmentStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/augment.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));

        final NotificationEffectiveStatement notif = module
            .findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow();

        final AugmentEffectiveStatement grpAug = notif
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(AugmentEffectiveStatement.class).orElseThrow();
        final AugmentEffectiveStatement contAug = notif
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(AugmentEffectiveStatement.class).orElseThrow();

        assertSame(contAug, grpAug);
    }

    @Test
    public void testCaseStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/case.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));

        final NotificationEffectiveStatement notif = module
            .findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow();

        final CaseEffectiveStatement grpBar = notif
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ChoiceEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(CaseEffectiveStatement.class).orElseThrow();
        final CaseEffectiveStatement contBar = notif
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ChoiceEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(CaseEffectiveStatement.class).orElseThrow();

        assertSame(contBar, grpBar);
    }

    @Test
    public void testChoiceStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/choice.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));

        final NotificationEffectiveStatement notif = module
            .findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow();

        final ChoiceEffectiveStatement grpBar = notif
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ChoiceEffectiveStatement.class).orElseThrow();
        final ChoiceEffectiveStatement contBar = notif
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ChoiceEffectiveStatement.class).orElseThrow();

        assertSame(contBar, grpBar);
    }

    @Test
    public void testGroupingStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/grouping.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));

        final NotificationEffectiveStatement notif = module
            .findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow();

        final GroupingEffectiveStatement grpBar = notif
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow();
        final GroupingEffectiveStatement contBar = notif
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow();

        assertSame(contBar, grpBar);
    }

    @Test
    public void testLeafStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/leaf.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));
        assertNotNull(module);

        final NotificationEffectiveStatement notif = module
            .findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow();

        final LeafEffectiveStatement grpBar = notif
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();
        final LeafEffectiveStatement contBar = notif
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();

        assertSame(contBar, grpBar);
    }

    @Test
    public void testLeafListStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/leaflist.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));

        final NotificationEffectiveStatement notif = module
            .findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow();

        final LeafListEffectiveStatement grpBar = notif
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(LeafListEffectiveStatement.class).orElseThrow();
        final LeafListEffectiveStatement contBar = notif
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(LeafListEffectiveStatement.class).orElseThrow();

        assertSame(contBar, grpBar);
    }

    @Test
    public void testListStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/list.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));

        final NotificationEffectiveStatement notif = module
            .findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow();

        final ListEffectiveStatement grpBar = notif
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ListEffectiveStatement.class).orElseThrow();
        final ListEffectiveStatement contBar = notif
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ListEffectiveStatement.class).orElseThrow();

        assertSame(contBar, grpBar);
    }

    @Test
    public void testTypedefStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/typedef.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));

        final TypedefEffectiveStatement grpBar = module
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(TypedefEffectiveStatement.class).orElseThrow();
        final TypedefEffectiveStatement contBar = module
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(TypedefEffectiveStatement.class).orElseThrow();

        assertSame(contBar, grpBar);
    }

    @Test
    public void testUsesStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/uses.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));
        assertNotNull(module);
        final List<GroupingEffectiveStatement> groupings = module
            .streamEffectiveSubstatements(GroupingEffectiveStatement.class).collect(Collectors.toList());
        assertEquals(2, groupings.size());
        final ContainerEffectiveStatement grpFoo = groupings.get(1)
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        final ContainerEffectiveStatement foo = module
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        // The statements do not significantly differ, hence they end up being reused completely
        assertSame(foo, grpFoo);
    }
}
