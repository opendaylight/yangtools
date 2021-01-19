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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

public class YT1208Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");

    @Test
    public void testSubstatementReuse() throws Exception {
        final ModuleEffectiveStatement foo = StmtTestUtils.parseYangSources("/bugs/YT1208/").getModuleStatements()
            .get(QNameModule.create(URI.create("foo")));
        assertNotNull(foo);
        final ContainerEffectiveStatement fooBar =
            foo.findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
                .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        final ContainerEffectiveStatement bar =
            foo.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertSame(fooBar.effectiveSubstatements(), bar.effectiveSubstatements());
    }
}
