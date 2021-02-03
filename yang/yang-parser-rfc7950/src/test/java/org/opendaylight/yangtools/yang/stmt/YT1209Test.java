/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;

public class YT1209Test {
    @Test
    public void testWhenStatementReuse() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/YT1209/when.yang")
            .getModuleStatement(QNameModule.create(URI.create("foo")));

        final LeafEffectiveStatement grpFoo = module
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();
        final LeafEffectiveStatement foo = module
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow();

        // The statements should not be the same due SchemaPath being part of LeafSchemaNode
        assertNotSame(foo, grpFoo);
        // The statements are instantiated in the same module, hence they should have the same argument
        assertSame(foo.argument(), grpFoo.argument());
        // The statements' when substatement should be reused
        assertSame(foo.findFirstEffectiveSubstatement(WhenEffectiveStatement.class).orElseThrow(),
            grpFoo.findFirstEffectiveSubstatement(WhenEffectiveStatement.class).orElseThrow());
    }
}
