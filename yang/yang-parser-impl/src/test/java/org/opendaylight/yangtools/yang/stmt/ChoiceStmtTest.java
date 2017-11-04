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
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class ChoiceStmtTest {

    private static final StatementStreamSource CHOICE_MODULE = sourceForResource("/model/foo.yang");
    private static final StatementStreamSource IMPORTED_MODULE1 = sourceForResource("/model/bar.yang");
    private static final StatementStreamSource IMPORTED_MODULE2 = sourceForResource("/model/baz.yang");
    private static final StatementStreamSource INCLUDED_MODULE = sourceForResource("/model/subfoo.yang");

    @Test
    public void choiceAndCaseTest() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = TestUtils.defaultParser();
        reactor.addSources(CHOICE_MODULE, IMPORTED_MODULE1, IMPORTED_MODULE2, INCLUDED_MODULE);

        final SchemaContext result = reactor.buildEffective();
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

        ChoiceCaseNode caseNode = choice.findCaseNodes("input").iterator().next();
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
