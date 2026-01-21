/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

class YT1410Test extends AbstractYangTest {
    @Test
    void testRFC6020() {
        assertInvalidSubstatementException(startsWith("choice statement does not allow choice substatements [at "),
            "/bugs/YT1410/foo.yang");
    }

    @Test
    void testRFC7950() {
        final var module = assertEffectiveModel("/bugs/YT1410/bar.yang").getModuleStatement(QName.create("bar", "bar"));
        final var one = assertInstanceOf(ChoiceEffectiveStatement.class,
            module.findSchemaTreeNode(QName.create("bar", "one")).orElseThrow());
        final var two = assertInstanceOf(CaseEffectiveStatement.class,
            one.findSchemaTreeNode(QName.create("bar", "two")).orElseThrow());
        assertInstanceOf(ChoiceEffectiveStatement.class,
            two.findSchemaTreeNode(QName.create("bar", "two")).orElseThrow());
    }
}
