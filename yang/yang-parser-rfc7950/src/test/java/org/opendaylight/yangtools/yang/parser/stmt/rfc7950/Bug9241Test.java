/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug9241Test {

    @Test
    public void testImplicitInputAndOutputInAction() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug9241/foo.yang");
        assertNotNull(schemaContext);

        final Module fooModule = schemaContext.findModule("foo", Revision.of("2017-10-13")).get();

        final ContainerSchemaNode actionCont = (ContainerSchemaNode) fooModule.getDataChildByName(QName.create(
                fooModule.getQNameModule(), "action-cont"));
        assertNotNull(actionCont);

        final ActionDefinition actionInCont = actionCont.getActions().iterator().next();

        final InputSchemaNode input = actionInCont.getInput();
        assertNotNull(input);
        assertEquals(1, input.getChildNodes().size());
        assertEquals(StatementSource.CONTEXT, ((EffectiveStatement<?, ?>) input).getStatementSource());

        final OutputSchemaNode output = actionInCont.getOutput();
        assertNotNull(output);
        assertEquals(1, output.getChildNodes().size());
        assertEquals(StatementSource.CONTEXT, ((EffectiveStatement<?, ?>) output).getStatementSource());
    }
}
