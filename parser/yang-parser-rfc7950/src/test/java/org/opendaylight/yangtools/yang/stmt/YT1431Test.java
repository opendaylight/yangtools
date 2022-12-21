/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

class YT1431Test {
    @Test
    void testUnsupportedChoiceLeaf() throws Exception {
        final var module = StmtTestUtils.parseYangSource("/bugs/YT1431/foo.yang", Set.of())
            .findModuleStatement(QName.create("foo", "foo"))
            .orElseThrow();
        final var choice = module.findFirstEffectiveSubstatement(ChoiceEffectiveStatement.class).orElseThrow();
        assertEquals(List.of(), choice.effectiveSubstatements());
    }

    @Test
    void testUnsupportedChoiceLeafAugment() throws Exception {
        final var module = StmtTestUtils.parseYangSource("/bugs/YT1431/bar.yang", Set.of())
            .findModuleStatement(QName.create("bar", "bar"))
            .orElseThrow();
        final var choice = module.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ChoiceEffectiveStatement.class)
            .orElseThrow();
        assertEquals(List.of(), choice.effectiveSubstatements());
    }
}
