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

import java.util.Date;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug9241Test {

    @Test
    public void testImplicitInputAndOutputInAction() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug9241/foo.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-10-13");

        final Module fooModule = schemaContext.findModuleByName("foo", revision);
        assertNotNull(fooModule);

        final ContainerSchemaNode actionCont = (ContainerSchemaNode) fooModule.getDataChildByName(QName.create(
                fooModule.getQNameModule(), "action-cont"));
        assertNotNull(actionCont);

        final ActionDefinition actionInCont = actionCont.getActions().iterator().next();

        final ContainerSchemaNode input = actionInCont.getInput();
        assertNotNull(input);
        assertEquals(1, input.getChildNodes().size());
        assertEquals(StatementSource.CONTEXT, ((EffectiveStatement<?, ?>) input).getDeclared().getStatementSource());

        final ContainerSchemaNode output = actionInCont.getOutput();
        assertNotNull(output);
        assertEquals(1, output.getChildNodes().size());
        assertEquals(StatementSource.CONTEXT, ((EffectiveStatement<?, ?>) output).getDeclared().getStatementSource());
    }
}
