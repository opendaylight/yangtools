/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug9241Test extends AbstractYangTest {

    @Test
    void testImplicitInputAndOutputInAction() {
        final var context = assertEffectiveModel("/rfc7950/bug9241/foo.yang");

        final var fooModule = context.findModule("foo", Revision.of("2017-10-13")).orElseThrow();

        final var actionCont = assertInstanceOf(ContainerSchemaNode.class,
            fooModule.getDataChildByName(QName.create(fooModule.getQNameModule(), "action-cont")));

        final var actionInCont = actionCont.getActions().iterator().next();

        final var input = actionInCont.getInput();
        assertEquals(1, input.getChildNodes().size());
        assertEquals(StatementOrigin.CONTEXT, ((EffectiveStatement<?, ?>) input).statementOrigin());

        final var output = actionInCont.getOutput();
        assertEquals(1, output.getChildNodes().size());
        assertEquals(StatementOrigin.CONTEXT, ((EffectiveStatement<?, ?>) output).statementOrigin());
    }
}
