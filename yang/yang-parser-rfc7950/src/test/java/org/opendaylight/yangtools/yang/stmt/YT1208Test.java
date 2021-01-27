/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

public class YT1208Test {
    @Test
    public void testGroupingStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1208/grouping.yang")
            .getModuleStatements()
            .get(QNameModule.create(URI.create("foo")));
        assertNotNull(module);

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
            .getModuleStatements()
            .get(QNameModule.create(URI.create("foo")));
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
}
