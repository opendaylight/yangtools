/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;

class YT1209Test extends AbstractYangTest {
    @Test
    void testWhenStatementReuse() {
        final var module = assertEffectiveModel("/bugs/YT1209/when.yang").getModuleStatement(QNameModule.of("foo"));

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
