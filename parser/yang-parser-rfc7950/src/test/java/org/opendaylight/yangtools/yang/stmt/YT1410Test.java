/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

public class YT1410Test extends AbstractYangTest {
    @Test
    public void testRFC6020() {
        assertInvalidSubstatementException(
            startsWith("CHOICE is not valid for CHOICE. Error in module foo (QNameModule{ns=foo}) [at "),
            "/bugs/YT1410/foo.yang");
    }

    @Test
    public void testRFC7950() {
        final var module = assertEffectiveModel("/bugs/YT1410/bar.yang").getModuleStatement(QName.create("bar", "bar"));
        final var one = module.findSchemaTreeNode(QName.create("bar", "one")).orElseThrow();
        assertThat(one, instanceOf(ChoiceEffectiveStatement.class));
        final var two = ((ChoiceEffectiveStatement) one).findSchemaTreeNode(QName.create("bar", "two")).orElseThrow();
        assertThat(two, instanceOf(CaseEffectiveStatement.class));
        assertThat(((CaseEffectiveStatement) two).findSchemaTreeNode(QName.create("bar", "two")).orElseThrow(),
            instanceOf(ChoiceEffectiveStatement.class));
    }
}
