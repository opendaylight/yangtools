/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class ChoiceStmtTest {
    @Test
    public void choiceAndCaseTest() throws ReactorException, YangSyntaxErrorException, URISyntaxException, IOException {
        final SchemaContext result = StmtTestUtils.parseYangSources("/model");
        assertNotNull(result);

        final Module testModule = result.findModules("foo").iterator().next();
        assertNotNull(testModule);

        final ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(QName.create(
                testModule.getQNameModule(), "transfer"));
        assertNotNull(container);

        final ChoiceSchemaNode choice = (ChoiceSchemaNode) container.getDataChildByName(QName.create(
                testModule.getQNameModule(), "how"));
        assertNotNull(choice);
        assertEquals(5, choice.getCases().size());

        CaseSchemaNode caseNode = choice.findCaseNodes("input").iterator().next();
        assertNotNull(caseNode);
        caseNode = choice.findCaseNodes("output").iterator().next();
        assertNotNull(caseNode);
        caseNode = choice.findCaseNodes("interval").iterator().next();
        assertNotNull(caseNode);
        caseNode = choice.findCaseNodes("daily").iterator().next();
        assertNotNull(caseNode);
        caseNode = choice.findCaseNodes("manual").iterator().next();
        assertNotNull(caseNode);
        assertEquals("interval", choice.getDefaultCase().get().getQName().getLocalName());
    }
}
