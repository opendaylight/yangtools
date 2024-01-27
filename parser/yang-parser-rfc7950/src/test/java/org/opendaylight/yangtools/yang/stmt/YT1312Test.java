/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;

class YT1312Test extends AbstractYangTest {
    @Test
    void testRefineDefault() {
        final var module = assertEffectiveModel("/bugs/YT1312/foo.yang").getModuleStatement(QNameModule.of("foo"));

        final LeafListEffectiveStatement grpFoo = module
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(LeafListEffectiveStatement.class).orElseThrow();
        final LeafListEffectiveStatement foo = module
            .findFirstEffectiveSubstatement(LeafListEffectiveStatement.class).orElseThrow();

        assertNotSame(foo, grpFoo);
        assertEquals(Optional.empty(), grpFoo.findFirstEffectiveSubstatementArgument(DefaultEffectiveStatement.class));
        assertEquals(Optional.of("abc"), foo.findFirstEffectiveSubstatementArgument(DefaultEffectiveStatement.class));
    }
}
