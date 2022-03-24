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

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

public class YT1195Test extends AbstractYangTest {
    @Test
    public void testKeyStatementReuse() {
        final ModuleEffectiveStatement module = assertEffectiveModel("/bugs/YT1195/key.yang")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));

        final ListEffectiveStatement grpFoo = module
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ListEffectiveStatement.class).orElseThrow();
        final ListEffectiveStatement foo = module
            .findFirstEffectiveSubstatement(ListEffectiveStatement.class).orElseThrow();

        // The statements should not be the same due history being part of ListSchemaNode
        assertNotSame(foo, grpFoo);
        // The statements are instantiated in the same module, hence they should have the same argument
        assertSame(foo.argument(), grpFoo.argument());
        // The statements' key substatement should be reused
        assertSame(foo.findFirstEffectiveSubstatement(KeyEffectiveStatement.class).orElseThrow(),
            grpFoo.findFirstEffectiveSubstatement(KeyEffectiveStatement.class).orElseThrow());
    }
}
